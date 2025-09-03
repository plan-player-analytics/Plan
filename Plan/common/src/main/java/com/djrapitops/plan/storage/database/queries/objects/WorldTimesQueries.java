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
package com.djrapitops.plan.storage.database.queries.objects;

import com.djrapitops.plan.delivery.domain.World;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link WorldTimes} objects.
 *
 * @author AuroraLS3
 */
public class WorldTimesQueries {

    private static final String WORLD_COLUMN = "world";
    private static final String SELECT_WORLD_TIMES_JOIN_WORLD_NAME = WorldTable.TABLE_NAME + '.' + WorldTable.NAME + " as " + WORLD_COLUMN +
            FROM + WorldTimesTable.TABLE_NAME +
            INNER_JOIN + WorldTable.TABLE_NAME + " on " + WorldTable.TABLE_NAME + '.' + WorldTable.ID + "=" + WorldTimesTable.WORLD_ID;
    private static final String SELECT_WORLD_TIMES_STATEMENT_START = SELECT +
            "SUM(" + WorldTimesTable.SURVIVAL + ") as survival, " +
            "SUM(" + WorldTimesTable.CREATIVE + ") as creative, " +
            "SUM(" + WorldTimesTable.ADVENTURE + ") as adventure, " +
            "SUM(" + WorldTimesTable.SPECTATOR + ") as spectator, ";

    private WorldTimesQueries() {
        /* Static method class */
    }

    /**
     * Sum total playtime per world on a server.
     *
     * @param serverUUID Server UUID of the Plan server.
     * @return WorldTimes with world name - playtime ms information.
     */
    public static Query<WorldTimes> fetchServerTotalWorldTimes(ServerUUID serverUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                SELECT_WORLD_TIMES_JOIN_WORLD_NAME +
                WHERE + WorldTimesTable.TABLE_NAME + '.' + WorldTimesTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + WORLD_COLUMN;

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                WorldTimes worldTimes = new WorldTimes();
                while (set.next()) {
                    String worldName = set.getString(WORLD_COLUMN);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                }
                return worldTimes;
            }
        };
    }

    /**
     * Sum total playtime per world on all servers.
     *
     * @param playerUUID UUID of the player.
     * @return WorldTimes with world name - playtime ms information.
     */
    public static Query<WorldTimes> fetchPlayerTotalWorldTimes(UUID playerUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                SELECT_WORLD_TIMES_JOIN_WORLD_NAME +
                WHERE + WorldTimesTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + WORLD_COLUMN;

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public WorldTimes processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                WorldTimes worldTimes = new WorldTimes();
                while (set.next()) {
                    String worldName = set.getString(WORLD_COLUMN);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                }
                return worldTimes;
            }
        };
    }

    /**
     * Find total world times of the player on servers.
     *
     * @param playerUUID UUID of the player.
     * @return Map: Server UUID - WorldTimes total for the server
     */
    public static Query<Map<ServerUUID, WorldTimes>> fetchPlayerWorldTimesOnServers(UUID playerUUID) {
        String sql = SELECT_WORLD_TIMES_STATEMENT_START +
                "s." + ServerTable.SERVER_UUID + ',' +
                SELECT_WORLD_TIMES_JOIN_WORLD_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on " + WorldTimesTable.TABLE_NAME + '.' + WorldTimesTable.SERVER_ID + "=s." + ServerTable.ID +
                WHERE + WorldTimesTable.TABLE_NAME + '.' + WorldTimesTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                GROUP_BY + WORLD_COLUMN + ",s." + ServerTable.SERVER_UUID;

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Map<ServerUUID, WorldTimes> processResults(ResultSet set) throws SQLException {
                String[] gms = GMTimes.getGMKeyArray();

                Map<ServerUUID, WorldTimes> worldTimesMap = new HashMap<>();
                while (set.next()) {
                    ServerUUID serverUUID = ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID));
                    WorldTimes worldTimes = worldTimesMap.getOrDefault(serverUUID, new WorldTimes());
                    String worldName = set.getString(WORLD_COLUMN);

                    GMTimes gmTimes = extractGMTimes(set, gms);

                    worldTimes.setGMTimesForWorld(worldName, gmTimes);
                    worldTimesMap.put(serverUUID, worldTimes);
                }
                return worldTimesMap;
            }
        };
    }

    private static GMTimes extractGMTimes(ResultSet set, String[] gms) throws SQLException {
        Map<String, Long> gmMap = new HashMap<>();
        for (String gameMode : gms) {
            gmMap.put(gameMode, set.getLong(gameMode));
        }
        return new GMTimes(gmMap);
    }

    public static Query<GMTimes> fetchGMTimes(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT +
                "SUM(" + WorldTimesTable.SURVIVAL + ") as SURVIVAL," +
                "SUM(" + WorldTimesTable.CREATIVE + ") as CREATIVE," +
                "SUM(" + WorldTimesTable.ADVENTURE + ") as ADVENTURE," +
                "SUM(" + WorldTimesTable.SPECTATOR + ") as SPECTATOR" +
                FROM + WorldTimesTable.TABLE_NAME + " w1" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s1 on s1." + SessionsTable.ID + '=' + WorldTimesTable.SESSION_ID +
                WHERE + "w1." + WorldTimesTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SESSION_END + "<=?";

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public GMTimes processResults(ResultSet set) throws SQLException {
                return set.next() ? extractGMTimes(set, GMTimes.getGMKeyArray()) : new GMTimes();
            }
        };
    }

    public static QueryStatement<Set<World>> fetchWorlds() {
        String worldNameSql = SELECT + '*' + FROM + WorldTable.TABLE_NAME;
        return new QueryAllStatement<>(worldNameSql) {
            @Override
            public Set<World> processResults(ResultSet set) throws SQLException {
                Set<World> worlds = new HashSet<>();
                while (set.next()) {
                    worlds.add(new World(
                            set.getString(WorldTable.NAME),
                            ServerUUID.fromString(set.getString(WorldTable.SERVER_UUID))
                    ));
                }
                return worlds;
            }
        };
    }

    public static Query<List<WorldTimesTable.Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(WorldTimesTable.TABLE_NAME)
                .where(WorldTimesTable.ID + '>' + currentId)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, WorldTimesTable.Row::extract);
    }

    public static Query<List<WorldTable.Row>> fetchWorldRows(int currentId, int rowLimit) {
        String sql = Select.all(WorldTable.TABLE_NAME)
                .where(WorldTable.ID + '>' + currentId)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, WorldTable.Row::extract);
    }

}