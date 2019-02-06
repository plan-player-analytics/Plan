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
package com.djrapitops.plan.db.access.queries;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.KillsTable;
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.WorldTable;
import com.djrapitops.plan.db.sql.tables.WorldTimesTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Static method class for queries that count together counts for a player on a per server basis.
 * <p>
 * Example:
 * Fetch how much a player has played on servers
 *
 * @author Rsl1122
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
    public static Query<Map<UUID, Long>> lastSeenOnServers(UUID playerUUID) {
        String sql = "SELECT MAX(" + SessionsTable.SESSION_END + ") as last_seen, " +
                SessionsTable.SERVER_UUID +
                " FROM " + SessionsTable.TABLE_NAME +
                " WHERE " + SessionsTable.USER_UUID + "=?" +
                " GROUP BY " + SessionsTable.SERVER_UUID;
        return new QueryStatement<Map<UUID, Long>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, Long> processResults(ResultSet set) throws SQLException {
                Map<UUID, Long> lastSeenMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
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
    public static Query<Map<UUID, Integer>> playerKillCountOnServers(UUID playerUUID) {
        String sql = "SELECT COUNT(1) as kill_count, " + KillsTable.SERVER_UUID + " FROM " + KillsTable.TABLE_NAME +
                " WHERE " + KillsTable.KILLER_UUID + "=?" +
                " GROUP BY " + KillsTable.SERVER_UUID;
        return new QueryStatement<Map<UUID, Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> killCountMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
                    int lastSeen = set.getInt("kill_count");
                    killCountMap.put(serverUUID, lastSeen);
                }
                return killCountMap;
            }
        };
    }

    /**
     * Find mob kill count on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - Mob kill count
     */
    public static Query<Map<UUID, Integer>> mobKillCountOnServers(UUID playerUUID) {
        String sql = "SELECT SUM(" + SessionsTable.MOB_KILLS + ") as kill_count, " +
                SessionsTable.SERVER_UUID + " FROM " + SessionsTable.TABLE_NAME +
                " WHERE " + SessionsTable.USER_UUID + "=?" +
                " GROUP BY " + SessionsTable.SERVER_UUID;
        return new QueryStatement<Map<UUID, Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> killCountMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
                    int lastSeen = set.getInt("kill_count");
                    killCountMap.put(serverUUID, lastSeen);
                }
                return killCountMap;
            }
        };
    }

    /**
     * Find how many times a player killed the player on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - Mob kill count
     */
    public static Query<Map<UUID, Integer>> playerDeathCountOnServers(UUID playerUUID) {
        String sql = "SELECT COUNT(1) as death_count, " + KillsTable.SERVER_UUID + " FROM " + KillsTable.TABLE_NAME +
                " WHERE " + KillsTable.VICTIM_UUID + "=?" +
                " GROUP BY " + KillsTable.SERVER_UUID;
        return new QueryStatement<Map<UUID, Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> killCountMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
                    int lastSeen = set.getInt("death_count");
                    killCountMap.put(serverUUID, lastSeen);
                }
                return killCountMap;
            }
        };
    }

    public static Query<Map<UUID, Integer>> totalDeathCountOnServers(UUID playerUUID) {
        String sql = "SELECT SUM(" + SessionsTable.DEATHS + ") as death_count, " +
                SessionsTable.SERVER_UUID + " FROM " + SessionsTable.TABLE_NAME +
                " WHERE " + SessionsTable.USER_UUID + "=?" +
                " GROUP BY " + SessionsTable.SERVER_UUID;
        return new QueryStatement<Map<UUID, Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, Integer> processResults(ResultSet set) throws SQLException {
                Map<UUID, Integer> killCountMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(SessionsTable.SERVER_UUID));
                    int lastSeen = set.getInt("death_count");
                    killCountMap.put(serverUUID, lastSeen);
                }
                return killCountMap;
            }
        };
    }

    /**
     * Find total world times of the player on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - WorldTimes total for the server
     */
    public static Query<Map<UUID, WorldTimes>> worldTimesOnServers(UUID playerUUID) {
        String worldIDColumn = WorldTable.TABLE_NAME + "." + WorldTable.ID;
        String worldNameColumn = WorldTable.TABLE_NAME + "." + WorldTable.NAME + " as world";
        String sql = "SELECT " +
                "SUM(" + WorldTimesTable.SURVIVAL + ") as survival, " +
                "SUM(" + WorldTimesTable.CREATIVE + ") as creative, " +
                "SUM(" + WorldTimesTable.ADVENTURE + ") as adventure, " +
                "SUM(" + WorldTimesTable.SPECTATOR + ") as spectator, " +
                worldNameColumn +
                " FROM " + WorldTimesTable.TABLE_NAME +
                " INNER JOIN " + WorldTable.TABLE_NAME + " on " + worldIDColumn + "=" + WorldTimesTable.WORLD_ID +
                " WHERE " + WorldTimesTable.TABLE_NAME + "." + WorldTimesTable.USER_UUID + "=?" +
                " GROUP BY world, " + WorldTimesTable.SERVER_UUID;

        return new QueryStatement<Map<UUID, WorldTimes>>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<UUID, WorldTimes> processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                Map<UUID, WorldTimes> worldTimesMap = new HashMap<>();
                while (set.next()) {
                    UUID serverUUID = UUID.fromString(set.getString(WorldTimesTable.SERVER_UUID));
                    WorldTimes worldTimes = worldTimesMap.getOrDefault(serverUUID, new WorldTimes(new HashMap<>()));
                    String worldName = set.getString("world");

                    Map<String, Long> gmMap = new HashMap<>();
                    gmMap.put(gms[0], set.getLong("survival"));
                    gmMap.put(gms[1], set.getLong("creative"));
                    gmMap.put(gms[2], set.getLong("adventure"));
                    gmMap.put(gms[3], set.getLong("spectator"));
                    GMTimes gmTimes = new GMTimes(gmMap);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                    worldTimesMap.put(serverUUID, worldTimes);
                }
                return worldTimesMap;
            }
        };
    }
}