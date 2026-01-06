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

import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.PingTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.java.Lists;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for {@link Ping} objects.
 *
 * @author AuroraLS3
 */
public class PingQueries {

    private PingQueries() {
        /* Static method class */
    }

    /**
     * Query database for all Ping data.
     *
     * @return Map: Player UUID - List of ping data.
     */
    public static Query<Map<UUID, List<Ping>>> fetchAllPingData() {
        String sql = SELECT +
                PingTable.DATE + ',' +
                PingTable.MAX_PING + ',' +
                PingTable.MIN_PING + ',' +
                PingTable.AVG_PING + ',' +
                "u." + UsersTable.USER_UUID + " as uuid," +
                "s." + ServerTable.SERVER_UUID + " as server_uuid" +
                FROM + PingTable.TABLE_NAME + " p" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u.id=p." + PingTable.USER_ID +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s.id=p." + PingTable.SERVER_ID;
        return new QueryAllStatement<>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                return extractUserPings(set);
            }
        };
    }

    private static Map<UUID, List<Ping>> extractUserPings(ResultSet set) throws SQLException {
        Map<UUID, List<Ping>> userPings = new HashMap<>();

        while (set.next()) {
            UUID uuid = UUID.fromString(set.getString("uuid"));
            ServerUUID serverUUID = ServerUUID.fromString(set.getString("server_uuid"));
            long date = set.getLong(PingTable.DATE);
            double avgPing = set.getDouble(PingTable.AVG_PING);
            int minPing = set.getInt(PingTable.MIN_PING);
            int maxPing = set.getInt(PingTable.MAX_PING);

            List<Ping> pings = userPings.computeIfAbsent(uuid, Lists::create);
            pings.add(new Ping(date, serverUUID,
                    minPing,
                    maxPing,
                    avgPing));
        }

        return userPings;
    }

    /**
     * Query database for Ping data of a specific player.
     *
     * @param playerUUID UUID of the player.
     * @return List of Ping entries for this player.
     */
    public static Query<List<Ping>> fetchPingDataOfPlayer(UUID playerUUID) {
        String sql = SELECT + '*' + FROM + PingTable.TABLE_NAME + " p" +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.ID + "=p." + PingTable.SERVER_ID +
                WHERE + PingTable.USER_ID + "=" + UsersTable.SELECT_USER_ID;

        return new QueryStatement<>(sql, 10000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public List<Ping> processResults(ResultSet set) throws SQLException {
                List<Ping> pings = new ArrayList<>();

                while (set.next()) {
                    pings.add(new Ping(
                                    set.getLong(PingTable.DATE),
                                    ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                                    set.getInt(PingTable.MIN_PING),
                                    set.getInt(PingTable.MAX_PING),
                                    set.getDouble(PingTable.AVG_PING)
                            )
                    );
                }

                return pings;
            }
        };
    }

    public static Query<List<Ping>> fetchPingDataOfServer(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT +
                PingTable.DATE + ", " +
                PingTable.MAX_PING + ", " +
                PingTable.MIN_PING + ", " +
                PingTable.AVG_PING +
                FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + PingTable.DATE + ">=?" +
                AND + PingTable.DATE + "<=?";
        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public List<Ping> processResults(ResultSet set) throws SQLException {
                List<Ping> pings = new ArrayList<>();

                while (set.next()) {
                    long date = set.getLong(PingTable.DATE);
                    double avgPing = set.getDouble(PingTable.AVG_PING);
                    int minPing = set.getInt(PingTable.MIN_PING);
                    int maxPing = set.getInt(PingTable.MAX_PING);

                    pings.add(new Ping(date, serverUUID,
                            minPing,
                            maxPing,
                            avgPing));
                }

                return pings;
            }
        };
    }

    public static Query<Map<String, Ping>> fetchPingDataOfServerByGeolocation(ServerUUID serverUUID) {
        String selectPingByGeolocation = SELECT + "a." + GeoInfoTable.GEOLOCATION +
                ", MIN(" + PingTable.MIN_PING + ") as minPing" +
                ", MAX(" + PingTable.MAX_PING + ") as maxPing" +
                ", AVG(" + PingTable.AVG_PING + ") as avgPing" +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                INNER_JOIN + PingTable.TABLE_NAME + " sp on sp." + PingTable.USER_ID + "=a." + GeoInfoTable.USER_ID +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL +
                AND + "sp." + PingTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + "a." + GeoInfoTable.GEOLOCATION;

        return new QueryStatement<>(selectPingByGeolocation) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<String, Ping> processResults(ResultSet set) throws SQLException {
                // TreeMap to sort alphabetically
                Map<String, Ping> pingByGeolocation = new TreeMap<>();
                while (set.next()) {
                    Ping ping = new Ping(
                            0L,
                            serverUUID,
                            set.getInt("minPing"),
                            set.getInt("maxPing"),
                            (int) set.getDouble("avgPing")
                    );
                    pingByGeolocation.put(set.getString(GeoInfoTable.GEOLOCATION), ping);
                }
                return pingByGeolocation;
            }
        };
    }

    public static Query<Map<String, Ping>> fetchPingDataOfNetworkByGeolocation() {
        String selectPingByGeolocation = SELECT + "a." + GeoInfoTable.GEOLOCATION +
                ", MIN(" + PingTable.MIN_PING + ") as minPing" +
                ", MAX(" + PingTable.MAX_PING + ") as maxPing" +
                ", AVG(" + PingTable.AVG_PING + ") as avgPing" +
                FROM + GeoInfoTable.TABLE_NAME + " a" +
                // Super smart optimization https://stackoverflow.com/a/28090544
                // Join the last_used column, but only if there's a bigger one.
                // That way the biggest a.last_used value will have NULL on the b.last_used column and MAX doesn't need to be used.
                LEFT_JOIN + GeoInfoTable.TABLE_NAME + " b ON a." + GeoInfoTable.USER_ID + "=b." + GeoInfoTable.USER_ID + AND + "a." + GeoInfoTable.LAST_USED + "<b." + GeoInfoTable.LAST_USED +
                INNER_JOIN + PingTable.TABLE_NAME + " sp on sp." + PingTable.USER_ID + "=a." + GeoInfoTable.USER_ID +
                WHERE + "b." + GeoInfoTable.LAST_USED + IS_NULL +
                GROUP_BY + "a." + GeoInfoTable.GEOLOCATION;

        return new QueryAllStatement<>(selectPingByGeolocation) {
            @Override
            public Map<String, Ping> processResults(ResultSet set) throws SQLException {
                // TreeMap to sort alphabetically
                Map<String, Ping> pingByGeolocation = new TreeMap<>();
                while (set.next()) {
                    Ping ping = new Ping(
                            0L,
                            null,
                            set.getInt("minPing"),
                            set.getInt("maxPing"),
                            (int) set.getDouble("avgPing")
                    );
                    pingByGeolocation.put(set.getString(GeoInfoTable.GEOLOCATION), ping);
                }
                return pingByGeolocation;
            }
        };
    }

    public static Query<Double> averagePing(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "AVG(" + PingTable.AVG_PING + ") as average" + FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + PingTable.DATE + ">=?" +
                AND + PingTable.DATE + "<=?";

        return new QueryStatement<>(sql, 1000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setLong(2, after);
                statement.setLong(3, before);
            }

            @Override
            public Double processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getDouble("average") : -1.0;
            }
        };
    }

    public static Query<Double> averagePing(long after, long before) {
        String sql = SELECT + "AVG(" + PingTable.AVG_PING + ") as average" + FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + PingTable.DATE + ">=?" +
                AND + PingTable.DATE + "<=?";
        return db -> db.queryOptional(sql, set -> set.getDouble("average"), after, before).orElse(-1.0);
    }

    public static Query<List<PingTable.Row>> fetchRows(int currentId, int rowLimit) {
        String sql = Select.all(PingTable.TABLE_NAME)
                .where(PingTable.ID + '>' + currentId)
                .orderBy(PingTable.ID)
                .limit(rowLimit)
                .toString();
        return db -> db.queryList(sql, PingTable.Row::extract);
    }
}