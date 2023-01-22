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
package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Service for resolving json asynchronously in order to move database queries off server thread.
 *
 * @author AuroraLS3
 */
@Singleton
public class AsyncJSONResolverService {

    private final PlanConfig config;
    private final Processing processing;
    private final JSONStorage jsonStorage;
    private final Map<String, Future<JSONStorage.StoredJSON>> currentlyProcessing;
    private final Map<String, Long> previousUpdates;
    private final ReentrantLock accessLock; // Access lock prevents double processing same resource
    private final Formatter<Long> httpLastModifiedFormatter;

    @Inject
    public AsyncJSONResolverService(
            PlanConfig config,
            Formatters formatters,
            Processing processing,
            JSONStorage jsonStorage
    ) {
        this.config = config;
        this.processing = processing;
        this.jsonStorage = jsonStorage;

        currentlyProcessing = new ConcurrentHashMap<>();
        previousUpdates = new ConcurrentHashMap<>();
        accessLock = new ReentrantLock();

        httpLastModifiedFormatter = formatters.httpLastModifiedLong();
    }

    public <T> JSONStorage.StoredJSON resolve(
            Optional<Long> newerThanTimestamp, DataID dataID, ServerUUID serverUUID, Function<ServerUUID, T> creator
    ) {
        String identifier = dataID.of(serverUUID);
        Supplier<T> jsonCreator = () -> creator.apply(serverUUID);
        return getStoredOrCreateJSON(newerThanTimestamp, identifier, jsonCreator);
    }


    public <T> JSONStorage.StoredJSON resolve(
            Optional<Long> newerThanTimestamp, DataID dataID, Supplier<T> jsonCreator
    ) {
        String identifier = dataID.name();
        return getStoredOrCreateJSON(newerThanTimestamp, identifier, jsonCreator);
    }

    private <T> JSONStorage.StoredJSON getStoredOrCreateJSON(
            Optional<Long> givenTimestamp, String identifier, Supplier<T> jsonCreator
    ) {
        JSONStorage.StoredJSON storedJSON = null;
        Future<JSONStorage.StoredJSON> updatedJSON = null;
        if (givenTimestamp.isPresent()) {
            long timestamp = givenTimestamp.get();
            storedJSON = getNewFromCache(timestamp, identifier);
            if (storedJSON != null) return storedJSON;

            // No new enough version, let's refresh and send old version of the file
            updatedJSON = scheduleJSONForUpdate(timestamp, identifier, jsonCreator);
            storedJSON = getOldFromCache(timestamp, identifier).orElse(null);
        }

        if (storedJSON != null) {
            return storedJSON; // Found old from cache
        } else {
            // Update not performed if the last update was recent and the file is deleted before next update
            // Fall back to waiting for the updated file if old version of the file doesn't exist.
            if (updatedJSON == null) {
                updatedJSON = submitToProcessing(identifier, jsonCreator);
            }
            return waitAndGetUpdated(updatedJSON);
        }
    }

    private JSONStorage.StoredJSON waitAndGetUpdated(Future<JSONStorage.StoredJSON> updatedJSON) {
        // If there is no version available, block thread until the new finishes being generated.
        try {
            return updatedJSON.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

    private Optional<JSONStorage.StoredJSON> getOldFromCache(long newerThanTimestamp, String identifier) {
        return jsonStorage.fetchJsonMadeBefore(identifier, newerThanTimestamp);
    }

    private JSONStorage.StoredJSON getNewFromCache(long newerThanTimestamp, String identifier) {
        return jsonStorage.fetchExactJson(identifier, newerThanTimestamp)
                .orElseGet(() -> jsonStorage.fetchJsonMadeAfter(identifier, newerThanTimestamp)
                        .orElse(null));
    }

    private <T> Future<JSONStorage.StoredJSON> scheduleJSONForUpdate(long newerThanTimestamp, String identifier, Supplier<T> jsonCreator) {
        long updateThreshold = config.get(WebserverSettings.REDUCED_REFRESH_BARRIER);

        Future<JSONStorage.StoredJSON> updatedJSON;
        accessLock.lock();
        try {
            // Check if the json is already being created
            updatedJSON = currentlyProcessing.get(identifier);
            if (updatedJSON == null && previousUpdates.getOrDefault(identifier, 0L) < newerThanTimestamp - updateThreshold) {
                // Submit a task to refresh the data if the json is old
                updatedJSON = submitToProcessing(identifier, jsonCreator);
                currentlyProcessing.put(identifier, updatedJSON);
            }
        } finally {
            accessLock.unlock();
        }
        return updatedJSON;
    }

    private <T> Future<JSONStorage.StoredJSON> submitToProcessing(String identifier, Supplier<T> jsonCreator) {
        return processing.submitNonCritical(() -> {
            JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, jsonCreator.get());
            currentlyProcessing.remove(identifier);
            jsonStorage.invalidateOlder(identifier, created.timestamp);
            previousUpdates.put(identifier, created.timestamp);
            return created;
        });
    }

    public Formatter<Long> getHttpLastModifiedFormatter() {
        return httpLastModifiedFormatter;
    }
}
