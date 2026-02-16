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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.LargeStoreQueries;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTable;
import com.djrapitops.plan.storage.database.sql.tables.WorldTimesTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Adds server_id field to worlds table.
 *
 * @author AuroraLS3
 * @see WorldsOptimizationPatch for removal of the field.
 */
public class WorldsServerIDPatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        String tableName = WorldTable.TABLE_NAME;
        String columnName = "server_id";

        // WorldsOptimizationPatch makes this patch incompatible with newer patch versions.
        return hasColumn(tableName, "server_uuid")
                || hasColumn(tableName, columnName)
                && allValuesHaveValueZero(tableName, columnName);
    }

    @Override
    protected void applyPatch() {
        Collection<ServerUUID> serverUUIDs = query(ServerQueries.fetchPlanServerInformation()).keySet();

        Map<ServerUUID, Collection<String>> worldsPerServer = new HashMap<>();
        for (ServerUUID serverUUID : serverUUIDs) {
            worldsPerServer.put(serverUUID, getWorldNamesOld(serverUUID));
        }

        execute(LargeStoreQueries.storeAllWorldNames(worldsPerServer));

        updateWorldTimesTableWorldIDs();
        executeSwallowingExceptions(DELETE_FROM + WorldTable.TABLE_NAME + WHERE + "server_id=0");
    }

    private Set<String> getWorldNamesOld(ServerUUID serverUUID) {
        String worldIDColumn = WorldTimesTable.TABLE_NAME + '.' + WorldTimesTable.WORLD_ID;
        String worldSessionIDColumn = WorldTimesTable.TABLE_NAME + '.' + WorldTimesTable.SESSION_ID;
        String sessionIDColumn = SessionsTable.TABLE_NAME + '.' + SessionsTable.ID;
        String sessionServerIDColumn = SessionsTable.TABLE_NAME + '.' + SessionsTable.SERVER_ID;
        String serverIDColumn = ServerTable.TABLE_NAME + '.' + ServerTable.ID;
        String serverUUIDColumn = ServerTable.TABLE_NAME + '.' + ServerTable.SERVER_UUID;

        String sql = SELECT + DISTINCT +
                WorldTable.NAME + FROM +
                WorldTable.TABLE_NAME +
                INNER_JOIN + WorldTimesTable.TABLE_NAME + " on " + worldIDColumn + "=" + WorldTable.TABLE_NAME + '.' + WorldTable.ID +
                INNER_JOIN + SessionsTable.TABLE_NAME + " on " + worldSessionIDColumn + "=" + sessionIDColumn +
                INNER_JOIN + ServerTable.TABLE_NAME + " on " + serverIDColumn + "=" + sessionServerIDColumn +
                WHERE + serverUUIDColumn + "=?" + lockForUpdate();

        return query(new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Set<String> processResults(ResultSet set) throws SQLException {
                Set<String> worldNames = new HashSet<>();
                while (set.next()) {
                    worldNames.add(set.getString(WorldTable.NAME));
                }
                return worldNames;
            }
        });
    }

    private void updateWorldTimesTableWorldIDs() {
        List<WorldObj> worldObjects = getWorldObjects();
        Map<WorldObj, List<WorldObj>> oldToNewMap =
                worldObjects.stream()
                        .filter(worldObj -> worldObj.serverId == 0)
                        .collect(Collectors.toMap(
                                Function.identity(),
                                oldWorld -> worldObjects.stream()
                                        .filter(worldObj -> worldObj.serverId != 0)
                                        .filter(worldObj -> worldObj.equals(oldWorld))
                                        .collect(Collectors.toList()
                                        )));

        String sql = "UPDATE " + WorldTimesTable.TABLE_NAME + " SET " +
                WorldTimesTable.WORLD_ID + "=?" +
                WHERE + WorldTimesTable.WORLD_ID + "=?" +
                AND + "server_id=?";
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<WorldObj, List<WorldObj>> entry : oldToNewMap.entrySet()) {
                    WorldObj old = entry.getKey();
                    for (WorldObj newWorld : entry.getValue()) {
                        statement.setInt(1, newWorld.id);
                        statement.setInt(2, old.id);
                        statement.setInt(3, newWorld.serverId);
                        statement.addBatch();
                    }
                }
            }
        });
    }

    private List<WorldObj> getWorldObjects() {
        String sql = SELECT + '*' + FROM + WorldTable.TABLE_NAME + lockForUpdate();
        return query(new QueryAllStatement<>(sql, 100) {
            @Override
            public List<WorldObj> processResults(ResultSet set) throws SQLException {
                List<WorldObj> objects = new ArrayList<>();
                while (set.next()) {
                    int worldID = set.getInt(WorldTable.ID);
                    int serverID = set.getInt("server_id");
                    String worldName = set.getString(WorldTable.NAME);
                    objects.add(new WorldObj(worldID, serverID, worldName));
                }
                return objects;
            }
        });
    }
}

class WorldObj {
    final int id;
    final int serverId;
    final String name;

    public WorldObj(int id, int serverId, String name) {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldObj worldObj = (WorldObj) o;
        return Objects.equals(name, worldObj.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", serverId=" + serverId +
                ", name='" + name + '\'' +
                '}';
    }
}
