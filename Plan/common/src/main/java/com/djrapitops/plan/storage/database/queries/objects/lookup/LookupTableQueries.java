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
package com.djrapitops.plan.storage.database.queries.objects.lookup;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.util.HashMap;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.FROM;
import static com.djrapitops.plan.storage.database.sql.building.Sql.SELECT;

/**
 * Queries for fetching lookup tables for different ids in databases that help combining data.
 *
 * @author AuroraLS3
 */
public class LookupTableQueries {

    private LookupTableQueries() {
        /* Static method class */
    }

    public static Query<LookupTable<UUID>> playerLookupTable() {
        String sql = SELECT + UsersTable.ID + ',' + UsersTable.USER_UUID + FROM + UsersTable.TABLE_NAME;
        return db -> new LookupTable<>(db.queryMap(sql, (set, map) ->
                map.put(
                        UUID.fromString(set.getString(UsersTable.USER_UUID)),
                        set.getInt(UsersTable.ID)
                ), HashMap::new));
    }

    public static Query<LookupTable<ServerUUID>> serverLookupTable() {
        String sql = SELECT + ServerTable.ID + ',' + ServerTable.SERVER_UUID + FROM + ServerTable.TABLE_NAME;
        return db -> new LookupTable<>(db.queryMap(sql, (set, map) ->
                map.put(
                        ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                        set.getInt(ServerTable.ID)
                ), HashMap::new));
    }

}
