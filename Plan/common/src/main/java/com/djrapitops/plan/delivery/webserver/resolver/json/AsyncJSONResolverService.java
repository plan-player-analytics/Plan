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
import com.djrapitops.plan.storage.json.JSONStorage;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
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

    private final Processing processing;
    private final JSONStorage jsonStorage;
    private final Map<String, Future<JSONStorage.StoredJSON>> currentlyProcessing;

    @Inject
    public AsyncJSONResolverService(
            Processing processing,
            JSONStorage jsonStorage
    ) {
        this.processing = processing;
        this.jsonStorage = jsonStorage;

        currentlyProcessing = new ConcurrentHashMap<>();
    }

    public <T> JSONStorage.StoredJSON resolve(long newerThanTimestamp, DataID dataID, UUID serverUUID, Function<UUID, T> creator) {
        String identifier = dataID.of(serverUUID);

        // Attempt to find a newer version of the json file from cache
        Optional<JSONStorage.StoredJSON> storedJSON = jsonStorage.fetchJsonMadeAfter(identifier, newerThanTimestamp);
        if (storedJSON.isPresent()) {
            return storedJSON.get();
        }
        // No new enough version, let's refresh and send old version of the file

        // Check if the json is already being created
        Future<JSONStorage.StoredJSON> updatedJSON = currentlyProcessing.get(identifier);
        if (updatedJSON == null) {
            // Submit a task to refresh the data if the json is old
            updatedJSON = processing.submitNonCritical(() -> {
                JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.apply(serverUUID));
                currentlyProcessing.remove(identifier);
                jsonStorage.invalidateOlder(identifier, created.timestamp);
                return created;
            });
            currentlyProcessing.put(identifier, updatedJSON);
        }

        // Get an old version from cache
        storedJSON = jsonStorage.fetchJsonMadeBefore(identifier, newerThanTimestamp);
        if (storedJSON.isPresent()) {
            return storedJSON.get();
        } else {
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
    }

    public <T> JSONStorage.StoredJSON resolve(long newerThanTimestamp, DataID dataID, Supplier<T> creator) {
        String identifier = dataID.name();

        // Attempt to find a newer version of the json file from cache
        Optional<JSONStorage.StoredJSON> storedJSON = jsonStorage.fetchJsonMadeAfter(identifier, newerThanTimestamp);
        if (storedJSON.isPresent()) {
            return storedJSON.get();
        }
        // No new enough version, let's refresh and send old version of the file

        // Check if the json is already being created
        Future<JSONStorage.StoredJSON> updatedJSON = currentlyProcessing.get(identifier);
        if (updatedJSON == null) {
            // Submit a task to refresh the data if the json is old
            updatedJSON = processing.submitNonCritical(() -> {
                JSONStorage.StoredJSON created = jsonStorage.storeJson(identifier, creator.get());
                currentlyProcessing.remove(identifier);
                jsonStorage.invalidateOlder(identifier, created.timestamp);
                return created;
            });
            currentlyProcessing.put(identifier, updatedJSON);
        }

        // Get an old version from cache
        storedJSON = jsonStorage.fetchJsonMadeBefore(identifier, newerThanTimestamp);
        if (storedJSON.isPresent()) {
            return storedJSON.get();
        } else {
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
    }

}
