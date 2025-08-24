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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class JSONMemoryStorageShim implements JSONStorage {

    private final PlanConfig config;
    private final JSONStorage underlyingStorage;

    private Cache<TimestampedIdentifier, StoredJSON> cache;

    public JSONMemoryStorageShim(
            PlanConfig config,
            JSONStorage underlyingStorage
    ) {
        this.config = config;
        this.underlyingStorage = underlyingStorage;
    }

    @Override
    public void enable() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(config.get(WebserverSettings.INVALIDATE_MEMORY_CACHE), TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public StoredJSON storeJson(String identifier, String json, long timestamp) {
        StoredJSON storedJSON = underlyingStorage.storeJson(identifier, json, timestamp);
        getCache().put(new TimestampedIdentifier(identifier, timestamp), storedJSON);
        return storedJSON;
    }

    public Cache<TimestampedIdentifier, StoredJSON> getCache() {
        if (cache == null) enable();
        return cache;
    }

    @Override
    public Optional<StoredJSON> fetchJSON(String identifier) {
        for (Map.Entry<TimestampedIdentifier, StoredJSON> entry : getCache().asMap().entrySet()) {
            if (entry.getKey().identifier.equalsIgnoreCase(identifier)) {
                return Optional.of(entry.getValue());
            }
        }
        Optional<StoredJSON> found = underlyingStorage.fetchJSON(identifier);
        found.ifPresent(storedJSON -> getCache().put(new TimestampedIdentifier(identifier, storedJSON.timestamp), storedJSON));
        return found;
    }

    @Override
    public Optional<StoredJSON> fetchExactJson(String identifier, long timestamp) {
        StoredJSON cached = getCache().getIfPresent(new TimestampedIdentifier(identifier, timestamp));
        if (cached != null) return Optional.of(cached);

        Optional<StoredJSON> found = underlyingStorage.fetchExactJson(identifier, timestamp);
        found.ifPresent(storedJSON -> getCache().put(new TimestampedIdentifier(identifier, timestamp), storedJSON));
        return found;
    }

    @Override
    public Optional<StoredJSON> fetchJsonMadeBefore(String identifier, long timestamp) {
        for (Map.Entry<TimestampedIdentifier, StoredJSON> entry : getCache().asMap().entrySet()) {
            TimestampedIdentifier key = entry.getKey();
            if (key.timestamp < timestamp && key.identifier.equalsIgnoreCase(identifier)) {
                return Optional.of(entry.getValue());
            }
        }

        Optional<StoredJSON> found = underlyingStorage.fetchJsonMadeBefore(identifier, timestamp);
        found.ifPresent(storedJSON -> getCache().put(new TimestampedIdentifier(identifier, storedJSON.timestamp), storedJSON));
        return found;
    }

    @Override
    public Optional<StoredJSON> fetchJsonMadeAfter(String identifier, long timestamp) {
        for (Map.Entry<TimestampedIdentifier, StoredJSON> entry : getCache().asMap().entrySet()) {
            TimestampedIdentifier key = entry.getKey();
            if (key.timestamp > timestamp && key.identifier.equalsIgnoreCase(identifier)) {
                return Optional.of(entry.getValue());
            }
        }
        Optional<StoredJSON> found = underlyingStorage.fetchJsonMadeAfter(identifier, timestamp);
        found.ifPresent(storedJSON -> getCache().put(new TimestampedIdentifier(identifier, storedJSON.timestamp), storedJSON));
        return found;
    }

    @Override
    public void invalidateOlder(String identifier, long timestamp) {
        Set<TimestampedIdentifier> toInvalidate = new HashSet<>();
        for (TimestampedIdentifier key : getCache().asMap().keySet()) {
            if (key.timestamp < timestamp && key.identifier.equalsIgnoreCase(identifier)) {
                toInvalidate.add(key);
            }
        }
        toInvalidate.forEach(getCache()::invalidate);

        underlyingStorage.invalidateOlder(identifier, timestamp);
    }

    @Override
    public Optional<Long> getTimestamp(String identifier) {
        for (TimestampedIdentifier key : getCache().asMap().keySet()) {
            if (key.identifier.equalsIgnoreCase(identifier)) {
                return Optional.of(key.timestamp);
            }
        }
        return Optional.empty();
    }

    static class TimestampedIdentifier {
        private final String identifier;
        private final long timestamp;

        public TimestampedIdentifier(String identifier, long timestamp) {
            this.identifier = identifier;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TimestampedIdentifier that = (TimestampedIdentifier) o;
            return timestamp == that.timestamp && identifier.equals(that.identifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, timestamp);
        }
    }
}
