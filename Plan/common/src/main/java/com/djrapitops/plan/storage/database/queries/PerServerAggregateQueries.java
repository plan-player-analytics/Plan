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
import com.djrapitops.plan.storage.database.sql.tables.KillsTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Static method class for queries that count together counts for a player on a per server basis.
 * <p>
 * Example:
 * Fetch how much a player has played on servers
 *
 * @author AuroraLS3
 */
public class PerServerAggregateQueries {

    private PerServerAggregateQueries() {
        /* Static method class */
    }

    /**
     * Find last seen date on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - Last seen epoch ms.
     */
    public static Query<Map<ServerUUID, Long>> lastSeenOnServers(UUID playerUUID) {
        String sql = SELECT + "MAX(" + SessionsTable.SESSION_END + ") as last_seen, " +
                ServerTable.SERVER_UUID +
                FROM + SessionsTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " se on se." + ServerTable.ID + '=' + SessionsTable.TABLE_NAME + '.' + SessionsTable.SERVER_ID +
                WHERE + SessionsTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + ServerTable.SERVER_UUID;
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<ServerUUID, Long> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Long> lastSeenMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    long lastSeen = set.getLong("last_seen");
                    lastSeenMap.put(serverUUID, lastSeen);
                }
                return lastSeenMap;
            }
        };
    }

    /**
     * Find player kill count on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - Player kill count
     */
    public static Query<Map<ServerUUID, Integer>> playerKillCountOnServers(UUID playerUUID) {
        String sql = SELECT + "COUNT(1) as kill_count, " + KillsTable.SERVER_UUID +
                FROM + KillsTable.TABLE_NAME +
                WHERE + KillsTable.KILLER_UUID + "=?" +
                GROUP_BY + KillsTable.SERVER_UUID;
        return getQueryForCountOf(playerUUID, sql, "kill_count");
    }

    /**
     * Find mob kill count on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - Mob kill count
     */
    public static Query<Map<ServerUUID, Integer>> mobKillCountOnServers(UUID playerUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.MOB_KILLS + ") as kill_count, " +
                ServerTable.SERVER_UUID + " as server_uuid" +
                FROM + SessionsTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " se on se." + ServerTable.ID + '=' + SessionsTable.TABLE_NAME + '.' + SessionsTable.SERVER_ID +
                WHERE + SessionsTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + ServerTable.SERVER_UUID;
        return getQueryForCountOf(playerUUID, sql, "kill_count");
    }

    public static Query<Map<ServerUUID, Integer>> totalDeathCountOnServers(UUID playerUUID) {
        String sql = SELECT + "SUM(" + SessionsTable.DEATHS + ") as death_count, " +
                ServerTable.SERVER_UUID + " as server_uuid" +
                FROM + SessionsTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " se on se." + ServerTable.ID + '=' + SessionsTable.TABLE_NAME + '.' + SessionsTable.SERVER_ID +
                WHERE + SessionsTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + ServerTable.SERVER_UUID;
        return getQueryForCountOf(playerUUID, sql, "death_count");
    }


    private static QueryStatement<Map<ServerUUID, Integer>> getQueryForCountOf(UUID playerUUID, String sql, String column) {
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<ServerUUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<ServerUUID, Integer> killCountMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString("server_uuid"));
                    int count = set.getInt(column);
                    killCountMap.put(serverUUID, count);
                }
                return killCountMap;
            }
        };
    }
}