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

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import org.apache.commons.text.TextStringBuilder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

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

        return new QueryAllStatement<Map<String, Integer>>(selectLatestJoinAddresses, 100) {
            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> joinAddresses = new TreeMap<>();
                while (set.next()) {
                    joinAddresses.put(set.getString(JoinAddressTable.JOIN_ADDRESS), set.getInt("total"));
                }
                return joinAddresses;
            }
        };
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

        return new QueryStatement<Map<String, Integer>>(selectLatestJoinAddresses, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> joinAddresses = new TreeMap<>();
                while (set.next()) {
                    joinAddresses.put(set.getString(JoinAddressTable.JOIN_ADDRESS), set.getInt("total"));
                }
                return joinAddresses;
            }
        };
    }

    public static QueryStatement<List<String>> allJoinAddresses() {
        String sql = SELECT + JoinAddressTable.JOIN_ADDRESS +
                FROM + JoinAddressTable.TABLE_NAME +
                ORDER_BY + JoinAddressTable.JOIN_ADDRESS + " ASC";
        return new QueryAllStatement<List<String>>(sql, 100) {
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

        return new QueryStatement<Set<Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (int i = 0; i < joinAddresses.size(); i++) {
                    statement.setString(i + 1, joinAddresses.get(i).toLowerCase());
                }
            }

            @Override
            public Set<Integer> processResults(ResultSet set) throws SQLException {
                return UserInfoQueries.extractUserIds(set, SessionsTable.USER_ID);
            }
        };
    }
}
