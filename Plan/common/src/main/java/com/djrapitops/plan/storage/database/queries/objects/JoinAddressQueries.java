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

import com.djrapitops.plan.delivery.domain.DateObj;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.queries.RowExtractors;
import com.djrapitops.plan.storage.database.sql.building.Select;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.dev.Untrusted;
import org.apache.commons.text.TextStringBuilder;
import org.jetbrains.annotations.VisibleForTesting;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

public class JoinAddressQueries {

    private JoinAddressQueries() {
        /* Static method class */
    }

    @VisibleForTesting
    public static Query<Map<String, Integer>> latestJoinAddresses() {
        String selectLatestJoinAddresses = SELECT +
                "COUNT(1) as total," +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + SessionsTable.TABLE_NAME + " a" +
                LEFT_JOIN + SessionsTable.TABLE_NAME + " b on a." + SessionsTable.ID + "<b." + SessionsTable.ID +
                AND + "a." + SessionsTable.USER_ID + "=b." + SessionsTable.USER_ID +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on j." + JoinAddressTable.ID + "=a." + SessionsTable.JOIN_ADDRESS_ID +
                WHERE + "b." + SessionsTable.ID + IS_NULL +
                GROUP_BY + JoinAddressTable.JOIN_ADDRESS +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";

        return db -> db.queryMap(selectLatestJoinAddresses, JoinAddressQueries::extractJoinAddressCounts, TreeMap::new);
    }

    private static void extractJoinAddressCounts(ResultSet set, Map<String, Integer> joinAddresses) throws SQLException {
        joinAddresses.put(set.getString(JoinAddressTable.JOIN_ADDRESS), set.getInt("total"));
    }

    private static void extractJoinAddress(ResultSet set, Map<UUID, String> joinAddresses) throws SQLException {
        joinAddresses.put(UUID.fromString(set.getString(UsersTable.USER_UUID)), set.getString(JoinAddressTable.JOIN_ADDRESS));
    }

    @VisibleForTesting
    public static Query<Map<String, Integer>> latestJoinAddresses(ServerUUID serverUUID) {
        String selectLatestSessionStarts = SELECT + SessionsTable.USER_ID + ",MAX(" + SessionsTable.SESSION_START + ") as max_start" +
                FROM + SessionsTable.TABLE_NAME + " max_s" +
                WHERE + "max_s." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + SessionsTable.USER_ID;
        String selectLatestJoinAddressIds = SELECT + SessionsTable.JOIN_ADDRESS_ID +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + "(" + selectLatestSessionStarts + ") q1 on q1." + SessionsTable.USER_ID + "=s." + SessionsTable.USER_ID +
                AND + "q1.max_start=s." + SessionsTable.SESSION_START;

        String selectJoinAddressCounts = SELECT +
                "COUNT(1) as total," +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + "(" + selectLatestJoinAddressIds + ") a" +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on j." + JoinAddressTable.ID + "=a." + SessionsTable.JOIN_ADDRESS_ID +
                GROUP_BY + JoinAddressTable.JOIN_ADDRESS +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";

        return db -> db.queryMap(selectJoinAddressCounts, JoinAddressQueries::extractJoinAddressCounts, TreeMap::new, serverUUID);
    }

    public static Query<Map<UUID, String>> latestJoinAddressesOfPlayers() {
        String selectLatestSessionStarts = SELECT + SessionsTable.USER_ID + ",MAX(" + SessionsTable.SESSION_START + ") as max_start" +
                FROM + SessionsTable.TABLE_NAME + " max_s" +
                GROUP_BY + SessionsTable.USER_ID;
        String selectLatestJoinAddressIds = SELECT + SessionsTable.JOIN_ADDRESS_ID + ",s." + SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + "(" + selectLatestSessionStarts + ") q1 on q1." + SessionsTable.USER_ID + "=s." + SessionsTable.USER_ID +
                AND + "q1.max_start=s." + SessionsTable.SESSION_START;

        String selectJoinAddress = SELECT +
                UsersTable.USER_UUID + ',' +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + "(" + selectLatestJoinAddressIds + ") a" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=a." + SessionsTable.USER_ID +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on j." + JoinAddressTable.ID + "=a." + SessionsTable.JOIN_ADDRESS_ID;

        return db -> db.queryMap(selectJoinAddress, JoinAddressQueries::extractJoinAddress, HashMap::new);
    }

    public static Query<Map<UUID, String>> latestJoinAddressesOfPlayers(ServerUUID serverUUID) {
        String selectLatestSessionStarts = SELECT + SessionsTable.USER_ID + ",MAX(" + SessionsTable.SESSION_START + ") as max_start" +
                FROM + SessionsTable.TABLE_NAME + " max_s" +
                WHERE + "max_s." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + SessionsTable.USER_ID;
        String selectLatestJoinAddressIds = SELECT + SessionsTable.JOIN_ADDRESS_ID + ",s." + SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + "(" + selectLatestSessionStarts + ") q1 on q1." + SessionsTable.USER_ID + "=s." + SessionsTable.USER_ID +
                AND + "q1.max_start=s." + SessionsTable.SESSION_START;

        String selectJoinAddress = SELECT +
                UsersTable.USER_UUID + ',' +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + "(" + selectLatestJoinAddressIds + ") a" +
                INNER_JOIN + UsersTable.TABLE_NAME + " u on u." + UsersTable.ID + "=a." + SessionsTable.USER_ID +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on j." + JoinAddressTable.ID + "=a." + SessionsTable.JOIN_ADDRESS_ID;

        return db -> db.queryMap(selectJoinAddress, JoinAddressQueries::extractJoinAddress, HashMap::new, serverUUID);
    }

    public static QueryStatement<List<String>> allJoinAddresses() {
        String sql = SELECT + JoinAddressTable.JOIN_ADDRESS +
                FROM + JoinAddressTable.TABLE_NAME +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";

        return new QueryAllStatement<>(sql, 100) {
            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> joinAddresses = new ArrayList<>();
                while (set.next()) joinAddresses.add(set.getString(JoinAddressTable.JOIN_ADDRESS));
                return joinAddresses;
            }
        };
    }

    public static QueryStatement<List<String>> allJoinAddresses(ServerUUID serverUUID) {
        String sql = SELECT + DISTINCT + JoinAddressTable.JOIN_ADDRESS +
                FROM + JoinAddressTable.TABLE_NAME + " j" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s ON s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";

        return new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> joinAddresses = new ArrayList<>();
                while (set.next()) joinAddresses.add(set.getString(JoinAddressTable.JOIN_ADDRESS));
                return joinAddresses;
            }
        };
    }

    public static Query<List<String>> uniqueJoinAddresses() {
        return db -> {
            List<String> addresses = db.query(allJoinAddresses());
            if (!addresses.contains(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP)) {
                addresses.add(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            }
            return addresses;
        };
    }

    public static Query<List<String>> uniqueJoinAddresses(ServerUUID serverUUID) {
        return db -> {
            List<String> addresses = db.query(allJoinAddresses(serverUUID));
            if (!addresses.contains(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP)) {
                addresses.add(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            }
            return addresses;
        };
    }

    public static Query<Set<Integer>> userIdsOfPlayersWithJoinAddresses(@Untrusted List<String> joinAddresses) {
        String sql = SELECT + DISTINCT + SessionsTable.USER_ID +
                FROM + JoinAddressTable.TABLE_NAME + " j" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                WHERE + JoinAddressTable.JOIN_ADDRESS + " IN (" +
                nParameters(joinAddresses.size()) +
                ')'; // Don't append addresses directly, SQL injection hazard

        return db -> db.querySet(sql, RowExtractors.getInt(SessionsTable.USER_ID), joinAddresses.toArray());
    }

    public static Query<List<DateObj<Map<String, Integer>>>> joinAddressesPerDay(ServerUUID serverUUID, long timezoneOffset, long after, long before, @Untrusted List<String> addressFilter) {
        return db -> {
            Sql sql = db.getSql();

            List<Integer> ids = db.query(joinAddressIds(addressFilter));
            if (ids != null && ids.isEmpty()) return List.of();

            String selectAddresses = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    JoinAddressTable.JOIN_ADDRESS + ',' +
                    SessionsTable.USER_ID +
                    ", COUNT(1) as count" +
                    FROM + SessionsTable.TABLE_NAME + " s" +
                    LEFT_JOIN + JoinAddressTable.TABLE_NAME + " j on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                    WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    AND + SessionsTable.SESSION_START + ">?" +
                    AND + SessionsTable.SESSION_START + "<=?" +
                    (ids == null ? "" : AND + "j." + JoinAddressTable.ID +
                            " IN (" + new TextStringBuilder().appendWithSeparators(ids, ",").get() + ")") +
                    GROUP_BY + "date,j." + JoinAddressTable.JOIN_ADDRESS + ',' + SessionsTable.USER_ID;

            return db.query(new QueryStatement<>(selectAddresses, 1000) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setLong(1, timezoneOffset);
                    statement.setString(2, serverUUID.toString());
                    statement.setLong(3, after);
                    statement.setLong(4, before);
                }

                @Override
                public List<DateObj<Map<String, Integer>>> processResults(ResultSet set) throws SQLException {
                    Map<Long, Map<String, Integer>> addressesByDate = new HashMap<>();
                    while (set.next()) {
                        long date = set.getLong("date");
                        String joinAddress = set.getString(JoinAddressTable.JOIN_ADDRESS);
                        Map<String, Integer> joinAddresses = addressesByDate.computeIfAbsent(date, k -> new TreeMap<>());
                        // We ignore the count and get the number of players instead of sessions
                        joinAddresses.compute(joinAddress, (key, oldValue) -> oldValue != null ? oldValue + 1 : 1);
                    }

                    return addressesByDate.entrySet()
                            .stream().map(entry -> new DateObj<>(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            });
        };
    }

    public static Query<List<Integer>> joinAddressIds(@Untrusted List<String> addresses) {
        return db -> {
            if (addresses.isEmpty()) return null;

            String selectJoinAddressIds = SELECT + JoinAddressTable.ID +
                    FROM + JoinAddressTable.TABLE_NAME +
                    WHERE + JoinAddressTable.JOIN_ADDRESS + " IN (" + Sql.nParameters(addresses.size()) + ")";
            return db.queryList(selectJoinAddressIds, set -> set.getInt(JoinAddressTable.ID), addresses);
        };
    }

    public static Query<List<DateObj<Map<String, Integer>>>> joinAddressesPerDay(long timezoneOffset, long after, long before, @Untrusted List<String> addressFilter) {
        return db -> {
            Sql sql = db.getSql();

            List<Integer> ids = db.query(joinAddressIds(addressFilter));
            if (ids != null && ids.isEmpty()) return List.of();

            String selectAddresses = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    JoinAddressTable.JOIN_ADDRESS + ',' +
                    SessionsTable.USER_ID +
                    ", COUNT(1) as count" +
                    FROM + SessionsTable.TABLE_NAME + " s" +
                    LEFT_JOIN + JoinAddressTable.TABLE_NAME + " j on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                    WHERE + SessionsTable.SESSION_START + ">?" +
                    AND + SessionsTable.SESSION_START + "<=?" +
                    (ids == null ? "" : AND + "j." + JoinAddressTable.ID +
                            " IN (" + new TextStringBuilder().appendWithSeparators(ids, ",").get() + ")") +
                    GROUP_BY + "date,j." + JoinAddressTable.JOIN_ADDRESS + ',' + SessionsTable.USER_ID;

            return db.query(new QueryStatement<>(selectAddresses, 1000) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setLong(1, timezoneOffset);
                    statement.setLong(2, after);
                    statement.setLong(3, before);
                }

                @Override
                public List<DateObj<Map<String, Integer>>> processResults(ResultSet set) throws SQLException {
                    Map<Long, Map<String, Integer>> addressesByDate = new HashMap<>();
                    while (set.next()) {
                        long date = set.getLong("date");
                        String joinAddress = set.getString(JoinAddressTable.JOIN_ADDRESS);
                        Map<String, Integer> joinAddresses = addressesByDate.computeIfAbsent(date, k -> new TreeMap<>());
                        // We ignore the count and get the number of players instead of sessions
                        joinAddresses.compute(joinAddress, (key, oldValue) -> oldValue != null ? oldValue + 1 : 1);
                    }

                    return addressesByDate.entrySet()
                            .stream().map(entry -> new DateObj<>(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            });
        };
    }

    public static Query<Optional<Integer>> getIdOfJoinAddress(String correctedAddress) {
        String sql = SELECT + JoinAddressTable.ID + FROM + JoinAddressTable.TABLE_NAME + WHERE + JoinAddressTable.JOIN_ADDRESS + "=?";
        return db -> db.queryOptional(sql,
                results -> results.getInt(JoinAddressTable.ID), correctedAddress);
    }

    public static Query<List<JoinAddressTable.Row>> fetchRows() {
        return db -> db.queryList(Select.all(JoinAddressTable.TABLE_NAME).toString(), JoinAddressTable.Row::extract);
    }
}
