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

import com.djrapitops.plan.delivery.domain.World;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebGroupToPermissionTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.WebPermissionTable;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public static Query<LookupTable<String>> joinAddressLookupTable() {
        return db -> new LookupTable<>(db.queryMap(Select.all(JoinAddressTable.TABLE_NAME).toString(),
                (set, map) -> map.put(
                        set.getString(JoinAddressTable.JOIN_ADDRESS),
                        set.getInt(JoinAddressTable.ID)
                )));
    }

    public static Query<LookupTable<World>> worldLookupTable() {
        return db -> new LookupTable<>(db.queryMap(Select.all(WorldTable.TABLE_NAME).toString(),
                (set, map) -> map.put(
                        new World(set.getString(WorldTable.NAME), ServerUUID.fromString(set.getString(WorldTable.SERVER_UUID))),
                        set.getInt(WorldTable.ID)
                )));
    }

    public static Query<LookupTable<String>> webGroupLookupTable() {
        return db -> new LookupTable<>(db.queryMap(Select.all(WebGroupTable.TABLE_NAME).toString(),
                (set, map) -> map.put(
                        set.getString(WebGroupTable.NAME),
                        set.getInt(WebGroupTable.ID)
                )));
    }

    public static Query<LookupTable<String>> webPermissionLookupTable() {
        return db -> new LookupTable<>(db.queryMap(Select.all(WebPermissionTable.TABLE_NAME).toString(),
                (set, map) -> map.put(
                        set.getString(WebPermissionTable.PERMISSION),
                        set.getInt(WebPermissionTable.ID)
                )));
    }

    public static Query<Map<Integer, List<Integer>>> webGroupToPermissionIds() {
        return db -> db.queryMap(WebGroupToPermissionTable.SELECT_IDS, (set, map) ->
                map.computeIfAbsent(set.getInt(WebGroupToPermissionTable.GROUP_ID), Lists::create)
                        .add(set.getInt(WebGroupToPermissionTable.PERMISSION_ID)));
    }

    public static Query<LookupTable<String>> webUserLookupTable() {
        return db -> new LookupTable<>(db.queryMap(Select.all(SecurityTable.TABLE_NAME).toString(),
                (set, map) -> map.put(
                        set.getString(SecurityTable.USERNAME),
                        set.getInt(SecurityTable.ID)
                )));
    }
}
