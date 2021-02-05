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
package com.djrapitops.plan.storage.json;

import com.google.gson.Gson;

import java.util.Objects;
import java.util.Optional;

/**
 * In charge of storing json somewhere for later retrieval.
 *
 * @author Rsl1122
 */
public interface JSONStorage {

    default StoredJSON storeJson(String identifier, String json) {
        return storeJson(identifier, json, System.currentTimeMillis());
    }

    default StoredJSON storeJson(String identifier, Object json) {
        return storeJson(identifier, new Gson().toJson(json));
    }

    StoredJSON storeJson(String identifier, String json, long timestamp);

    default StoredJSON storeJson(String identifier, Object json, long timestamp) {
        return storeJson(identifier, new Gson().toJson(json), timestamp);
    }

    Optional<StoredJSON> fetchJSON(String identifier);

    Optional<StoredJSON> fetchExactJson(String identifier, long timestamp);

    Optional<StoredJSON> fetchJsonMadeBefore(String identifier, long timestamp);

    Optional<StoredJSON> fetchJsonMadeAfter(String identifier, long timestamp);

    void invalidateOlder(String identifier, long timestamp);

    final class StoredJSON {
        public final String json;
        public final long timestamp;

        public StoredJSON(String json, long timestamp) {
            this.json = json;
            this.timestamp = timestamp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StoredJSON that = (StoredJSON) o;
            return timestamp == that.timestamp && Objects.equals(json, that.json);
        }

        @Override
        public int hashCode() {
            return Objects.hash(json, timestamp);
        }
    }
}
