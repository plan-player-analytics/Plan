/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.json.JSONStorage;
import com.djrapitops.plan.utilities.UnitSemaphoreAccessLock;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Service for resolving json asynchronously in order to move database queries off server thread.
 *
 * @author Rsl1122
 */
@Singleton
public class AsyncJSONResolverService {

    private final PlanConfig config;
    private final Processing processing;
    private final JSONStorage jsonStorage;
    private final Map<String, Future<JSONStorage.StoredJSON>> currentlyProcessing;
    private final Map<String, Long> previousUpdates;
    private final UnitSemaphoreAccessLock accessLock; // Access lock prevents double processing same resource

    @Inject
    public AsyncJSONResolverService(
            PlanConfig config,
            Processing processing,
            JSONStorage jsonStorage
    ) {
        this.config = config;
        this.processing = processing;
        this.jsonStorage = jsonStorage;

        currentlyProcessing = new ConcurrentHashMap<>();
        previousUpdates = new ConcurrentHashMap<>();
        accessLock = new UnitSemaphoreAccessLock();
    }

    public <T> JSONStorage.StoredJSON resolve(long newerThanTimestamp, DataID dataID, UUID serverUUID, Function<UUID, T> creator) {
        String identifier = dataID.of(serverUUID);

        // Attempt to find a newer version of the json file from cache
        JSONStorage.StoredJSON storedJSON = jsonStorage.fetchExactJson(identifier, newerThanTimestamp)
                .orElseGet(() -> jsonStorage.fetchJsonMadeAfter(identifier, newerThanTimestamp)
                        .orElse(null));
        if (storedJSON != null) {
            return storedJSON;
        }
        // No new enough version, let's refresh and send old version of the file

        long updateThreshold = config.get(WebserverSettings.REDUCED_REFRESH_BARRIER);

        // Check if the json is already being created
        Future<JSONStorage.StoredJSON> updatedJSON;
        accessLock.enter();
        try {
            updatedJSON = currentlyProcessing.get(identifier);
            if (updatedJSON == null && previousUpdates.getOrDefault(identifier, 0L) < newerThanTimestamp - updateThreshold) {
                // Submit a task to refresh the data if the json is old
                updatedJSON = processing.submitNonCritical(() -> {
                    JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.apply(serverUUID));
                    currentlyProcessing.remove(identifier);
                    jsonStorage.invalidateOlder(identifier, created.timestamp);
                    previousUpdates.put(identifier, created.timestamp);
                    return created;
                });
                currentlyProcessing.put(identifier, updatedJSON);
            }
        } finally {
            accessLock.exit();
        }

        // Get an old version from cache
        storedJSON = jsonStorage.fetchJsonMadeBefore(identifier, newerThanTimestamp).orElse(null);
        if (storedJSON != null) {
            return storedJSON;
        } else {
            // If there is no version available, block thread until the new finishes being generated.
            try {
                // Can be null if the last update was recent and the file is deleted before next update
                if (updatedJSON == null) {
                    updatedJSON = processing.submitNonCritical(() -> {
                        JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.apply(serverUUID));
                        currentlyProcessing.remove(identifier);
                        jsonStorage.invalidateOlder(identifier, created.timestamp);
                        previousUpdates.put(identifier, created.timestamp);
                        return created;
                    }); // TODO Refactor this spaghetti code
                }
                return updatedJSON.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public <T> JSONStorage.StoredJSON resolve(long newerThanTimestamp, DataID dataID, Supplier<T> creator) {
        String identifier = dataID.name();

        // Attempt to find a newer version of the json file from cache
        JSONStorage.StoredJSON storedJSON = jsonStorage.fetchExactJson(identifier, newerThanTimestamp)
                .orElseGet(() -> jsonStorage.fetchJsonMadeAfter(identifier, newerThanTimestamp)
                        .orElse(null));
        if (storedJSON != null) {
            return storedJSON;
        }
        // No new enough version, let's refresh and send old version of the file

        long updateThreshold = config.get(WebserverSettings.REDUCED_REFRESH_BARRIER);

        // Check if the json is already being created
        Future<JSONStorage.StoredJSON> updatedJSON;
        accessLock.enter();
        try {
            updatedJSON = currentlyProcessing.get(identifier);
            if (updatedJSON == null && previousUpdates.getOrDefault(identifier, 0L) < newerThanTimestamp - updateThreshold) {
                // Submit a task to refresh the data if the json is old
                updatedJSON = processing.submitNonCritical(() -> {
                    JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.get());
                    currentlyProcessing.remove(identifier);
                    jsonStorage.invalidateOlder(identifier, created.timestamp);
                    previousUpdates.put(identifier, created.timestamp);
                    return created;
                });
                currentlyProcessing.put(identifier, updatedJSON);
            }
        } finally {
            accessLock.exit();
        }

        // Get an old version from cache
        storedJSON = jsonStorage.fetchJsonMadeBefore(identifier, newerThanTimestamp).orElse(null);
        if (storedJSON != null) {
            return storedJSON;
        } else {
            // If there is no version available, block thread until the new finishes being generated.
            try {
                // Can be null if the last update was recent and the file is deleted before next update.
                if (updatedJSON == null) {
                    updatedJSON = processing.submitNonCritical(() -> {
                        JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.get());
                        currentlyProcessing.remove(identifier);
                        jsonStorage.invalidateOlder(identifier, created.timestamp);
                        previousUpdates.put(identifier, created.timestamp);
                        return created;
                    }); // TODO Refactor this spaghetti code
                }
                return updatedJSON.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            } catch (ExecutionException e) {
                throw new IllegalStateException(e);
            }
        }
    }

}
