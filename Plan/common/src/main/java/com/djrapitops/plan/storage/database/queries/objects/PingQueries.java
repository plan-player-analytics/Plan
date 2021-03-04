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
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.GeoInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.PingTable;
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
                PingTable.USER_UUID + ',' +
                PingTable.SERVER_UUID +
                FROM + PingTable.TABLE_NAME;
        return new QueryAllStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                return extractUserPings(set);
            }
        };
    }

    private static Map<UUID, List<Ping>> extractUserPings(ResultSet set) throws SQLException {
        Map<UUID, List<Ping>> userPings = new HashMap<>();

        while (set.next()) {
            UUID uuid = UUID.fromString(set.getString(PingTable.USER_UUID));
            UUID serverUUID = UUID.fromString(set.getString(PingTable.SERVER_UUID));
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
        String sql = SELECT + '*' + FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.USER_UUID + "=?";

        return new QueryStatement<List<Ping>>(sql, 10000) {
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
                                    UUID.fromString(set.getString(PingTable.SERVER_UUID)),
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

    public static Query<Map<UUID, List<Ping>>> fetchPingDataOfServer(UUID serverUUID) {
        String sql = SELECT +
                PingTable.DATE + ',' +
                PingTable.MAX_PING + ',' +
                PingTable.MIN_PING + ',' +
                PingTable.AVG_PING + ',' +
                PingTable.USER_UUID + ',' +
                PingTable.SERVER_UUID +
                FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_UUID + "=?";
        return new QueryStatement<Map<UUID, List<Ping>>>(sql, 100000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<UUID, List<Ping>> processResults(ResultSet set) throws SQLException {
                return extractUserPings(set);
            }
        };
    }

    public static Query<List<Ping>> fetchPingDataOfServer(long after, long before, UUID serverUUID) {
        String sql = SELECT +
                PingTable.DATE + ", " +
                PingTable.MAX_PING + ", " +
                PingTable.MIN_PING + ", " +
                PingTable.AVG_PING + ", " +
                PingTable.SERVER_UUID +
                FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_UUID + "=?" +
                AND + PingTable.DATE + ">=?" +
                AND + PingTable.DATE + "<=?";
        return new QueryStatement<List<Ping>>(sql, 1000) {
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
                    UUID serverUUID = UUID.fromString(set.getString(PingTable.SERVER_UUID));
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

    public static Query<Map<String, Ping>> fetchPingDataOfServerByGeolocation(UUID serverUUID) {
        String selectPingOfServer = SELECT +
                PingTable.MAX_PING + ", " +
                PingTable.MIN_PING + ", " +
                PingTable.AVG_PING + ", " +
                PingTable.USER_UUID + ", " +
                PingTable.SERVER_UUID +
                FROM + PingTable.TABLE_NAME;

        String selectGeolocations = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                GeoInfoTable.GEOLOCATION + ", " +
                GeoInfoTable.LAST_USED +
                FROM + GeoInfoTable.TABLE_NAME;
        String selectLatestGeolocationDate = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                "MAX(" + GeoInfoTable.LAST_USED + ") as m" +
                FROM + GeoInfoTable.TABLE_NAME +
                GROUP_BY + GeoInfoTable.USER_UUID;

        String selectPingByGeolocation = SELECT + GeoInfoTable.GEOLOCATION +
                ", MIN(" + PingTable.MIN_PING + ") as minPing" +
                ", MAX(" + PingTable.MAX_PING + ") as maxPing" +
                ", AVG(" + PingTable.AVG_PING + ") as avgPing" +
                FROM + "(" + selectGeolocations + ") AS q1" +
                INNER_JOIN + "(" + selectLatestGeolocationDate + ") AS q2 ON q1.uuid = q2.uuid" +
                INNER_JOIN + '(' + selectPingOfServer + ") sp on sp." + PingTable.USER_UUID + "=q1.uuid" +
                WHERE + GeoInfoTable.LAST_USED + "=m" +
                AND + "sp." + PingTable.SERVER_UUID + "=?" +
                GROUP_BY + GeoInfoTable.GEOLOCATION;

        return new QueryStatement<Map<String, Ping>>(selectPingByGeolocation) {
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
        String selectPingOfServer = SELECT +
                PingTable.MAX_PING + ", " +
                PingTable.MIN_PING + ", " +
                PingTable.AVG_PING + ", " +
                PingTable.USER_UUID + ", " +
                PingTable.SERVER_UUID +
                FROM + PingTable.TABLE_NAME;

        String selectGeolocations = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                GeoInfoTable.GEOLOCATION + ", " +
                GeoInfoTable.LAST_USED +
                FROM + GeoInfoTable.TABLE_NAME;
        String selectLatestGeolocationDate = SELECT +
                GeoInfoTable.USER_UUID + ", " +
                "MAX(" + GeoInfoTable.LAST_USED + ") as m" +
                FROM + GeoInfoTable.TABLE_NAME +
                GROUP_BY + GeoInfoTable.USER_UUID;

        String selectPingByGeolocation = SELECT + GeoInfoTable.GEOLOCATION +
                ", MIN(" + PingTable.MIN_PING + ") as minPing" +
                ", MAX(" + PingTable.MAX_PING + ") as maxPing" +
                ", AVG(" + PingTable.AVG_PING + ") as avgPing" +
                FROM + "(" +
                "(" + selectGeolocations + ") AS q1" +
                INNER_JOIN + "(" + selectLatestGeolocationDate + ") AS q2 ON q1.uuid = q2.uuid" +
                INNER_JOIN + '(' + selectPingOfServer + ") sp on sp." + PingTable.USER_UUID + "=q1.uuid)" +
                WHERE + GeoInfoTable.LAST_USED + "=m" +
                GROUP_BY + GeoInfoTable.GEOLOCATION;

        return new QueryAllStatement<Map<String, Ping>>(selectPingByGeolocation) {
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

    public static Query<Double> averagePing(long after, long before, UUID serverUUID) {
        String sql = SELECT + "AVG(" + PingTable.AVG_PING + ") as average" + FROM + PingTable.TABLE_NAME +
                WHERE + PingTable.SERVER_UUID + "=?" +
                AND + PingTable.DATE + ">=?" +
                AND + PingTable.DATE + "<=?";

        return new QueryStatement<Double>(sql, 1000) {
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
}