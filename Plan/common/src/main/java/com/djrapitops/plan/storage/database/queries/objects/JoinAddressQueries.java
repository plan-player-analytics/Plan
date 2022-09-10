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
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import org.apache.commons.text.TextStringBuilder;

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

    public static Query<Map<String, Integer>> latestJoinAddresses(ServerUUID serverUUID) {
        String selectLatestJoinAddresses = SELECT +
                "COUNT(1) as total," +
                JoinAddressTable.JOIN_ADDRESS +
                FROM + SessionsTable.TABLE_NAME + " a" +
                LEFT_JOIN + SessionsTable.TABLE_NAME + " b on a." + SessionsTable.ID + "<b." + SessionsTable.ID +
                AND + "a." + SessionsTable.USER_ID + "=b." + SessionsTable.USER_ID +
                AND + "a." + SessionsTable.SERVER_ID + "=b." + SessionsTable.SERVER_ID +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on j." + JoinAddressTable.ID + "=a." + SessionsTable.JOIN_ADDRESS_ID +
                WHERE + "b." + SessionsTable.ID + IS_NULL +
                AND + "a." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                GROUP_BY + JoinAddressTable.JOIN_ADDRESS +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";


        return db -> db.queryMap(selectLatestJoinAddresses, JoinAddressQueries::extractJoinAddressCounts, TreeMap::new, serverUUID);
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

    public static Query<List<String>> uniqueJoinAddresses() {
        return db -> {
            List<String> addresses = db.query(allJoinAddresses());
            if (!addresses.contains(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP)) {
                addresses.add(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            }
            return addresses;
        };
    }

    public static Query<Set<Integer>> userIdsOfPlayersWithJoinAddresses(List<String> joinAddresses) {
        String sql = SELECT + DISTINCT + SessionsTable.USER_ID +
                FROM + JoinAddressTable.TABLE_NAME + " j" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                WHERE + JoinAddressTable.JOIN_ADDRESS + " IN (" +
                new TextStringBuilder().appendWithSeparators(joinAddresses.stream().map(item -> '?').iterator(), ",") +
                ')'; // Don't append addresses directly, SQL injection hazard

        return db -> db.querySet(sql, RowExtractors.getInt(SessionsTable.USER_ID), joinAddresses.toArray());
    }

    public static Query<List<DateObj<Map<String, Integer>>>> joinAddressesPerDay(ServerUUID serverUUID, long timezoneOffset, long after, long before) {
        return db -> {
            Sql sql = db.getSql();

            String selectAddresses = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    JoinAddressTable.JOIN_ADDRESS +
                    ", COUNT(1) as count" +
                    FROM + SessionsTable.TABLE_NAME + " s" +
                    LEFT_JOIN + JoinAddressTable.TABLE_NAME + " j on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                    WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    AND + SessionsTable.SESSION_START + ">?" +
                    AND + SessionsTable.SESSION_START + "<=?" +
                    GROUP_BY + "date,j." + JoinAddressTable.ID;

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
                        int count = set.getInt("count");
                        Map<String, Integer> joinAddresses = addressesByDate.computeIfAbsent(date, k -> new TreeMap<>());
                        joinAddresses.put(joinAddress, count);
                    }

                    return addressesByDate.entrySet()
                            .stream().map(entry -> new DateObj<>(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            });
        };
    }

    public static Query<List<DateObj<Map<String, Integer>>>> joinAddressesPerDay(long timezoneOffset, long after, long before) {
        return db -> {
            Sql sql = db.getSql();

            String selectAddresses = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    JoinAddressTable.JOIN_ADDRESS +
                    ", COUNT(1) as count" +
                    FROM + SessionsTable.TABLE_NAME + " s" +
                    LEFT_JOIN + JoinAddressTable.TABLE_NAME + " j on s." + SessionsTable.JOIN_ADDRESS_ID + "=j." + JoinAddressTable.ID +
                    WHERE + SessionsTable.SESSION_START + ">?" +
                    AND + SessionsTable.SESSION_START + "<=?" +
                    GROUP_BY + "date,j." + JoinAddressTable.ID;

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
                        int count = set.getInt("count");
                        Map<String, Integer> joinAddresses = addressesByDate.computeIfAbsent(date, k -> new TreeMap<>());
                        joinAddresses.put(joinAddress, count);
                    }

                    return addressesByDate.entrySet()
                            .stream().map(entry -> new DateObj<>(entry.getKey(), entry.getValue()))
                            .collect(Collectors.toList());
                }
            });
        };
    }
}
