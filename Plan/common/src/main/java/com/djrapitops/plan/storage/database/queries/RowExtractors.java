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
package com.djrapitops.plan.storage.database.queries;

import com.djrapitops.plan.identification.ServerUUID;

import java.util.UUID;

public class RowExtractors {
    private RowExtractors() {}

    public static RowExtractor<Integer> getInt(String columnName) {
        return set -> {
            int value = set.getInt(columnName);
            return set.wasNull() ? null : value;
        };
    }

    public static RowExtractor<Long> getLong(String columnName) {
        return set -> {
            long value = set.getLong(columnName);
            return set.wasNull() ? null : value;
        };
    }

    public static RowExtractor<String> getString(String columnName) {
        return set -> set.getString(columnName);
    }

    public static RowExtractor<UUID> getUUID(String columnName) {
        return set -> UUID.fromString(set.getString(columnName));
    }

    public static RowExtractor<ServerUUID> getServerUUID(String columnName) {
        return set -> ServerUUID.fromString(set.getString(columnName));
    }

}
