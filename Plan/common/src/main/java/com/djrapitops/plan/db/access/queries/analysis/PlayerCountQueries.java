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
package com.djrapitops.plan.db.access.queries.analysis;

import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.Sql;
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.UserInfoTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Queries for server overview tab data.
 *
 * @author Rsl1122
 */
public class PlayerCountQueries {

    private PlayerCountQueries() {
        // Static method class
    }

    private static QueryStatement<Integer> queryPlayerCount(String sql, long after, long before, UUID serverUUID) {
        return new QueryStatement<Integer>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, before);
                statement.setLong(2, after);
                statement.setString(3, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("player_count") : 0;
            }
        };
    }

    public static Query<Integer> uniquePlayerCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "COUNT(DISTINCT " + SessionsTable.USER_UUID + ") as player_count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SESSION_END + "<=?" +
                AND + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SERVER_UUID + "=?";

        return queryPlayerCount(sql, after, before, serverUUID);
    }

    /**
     * Fetch a EpochMs - Count map of unique players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Accuracy of a day) - How many unique players played that day
     */
    public static Query<NavigableMap<Long, Integer>> uniquePlayerCounts(long after, long before, long timeZoneOffset, UUID serverUUID) {
        return database -> {
            Sql sql = database.getType().getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_UUID + ") as player_count" +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    AND + SessionsTable.SERVER_UUID + "=?" +
                    GROUP_BY + "date";

            return database.query(new QueryStatement<NavigableMap<Long, Integer>>(selectUniquePlayersPerDay, 100) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setLong(1, timeZoneOffset);
                    statement.setLong(2, before);
                    statement.setLong(3, after);
                    statement.setString(4, serverUUID.toString());
                }

                @Override
                public NavigableMap<Long, Integer> processResults(ResultSet set) throws SQLException {
                    NavigableMap<Long, Integer> uniquePerDay = new TreeMap<>();
                    while (set.next()) {
                        uniquePerDay.put(set.getLong("date"), set.getInt("player_count"));
                    }
                    return uniquePerDay;
                }
            });
        };
    }

    public static Query<Integer> newPlayerCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "COUNT(" + UserInfoTable.USER_UUID + ") as player_count" +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_UUID + "=?";

        return queryPlayerCount(sql, after, before, serverUUID);
    }

    /**
     * Fetch a EpochMs - Count map of new players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Accuracy of a day) - How many new players joined that day
     */
    public static Query<NavigableMap<Long, Integer>> newPlayerCounts(long after, long before, long timeZoneOffset, UUID serverUUID) {
        return database -> {
            Sql sql = database.getType().getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as player_count" +
                    FROM + UserInfoTable.TABLE_NAME +
                    WHERE + UserInfoTable.REGISTERED + "<=?" +
                    AND + UserInfoTable.REGISTERED + ">=?" +
                    AND + UserInfoTable.SERVER_UUID + "=?" +
                    GROUP_BY + "date";

            return database.query(new QueryStatement<NavigableMap<Long, Integer>>(selectNewPlayersQuery, 100) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    statement.setLong(1, timeZoneOffset);
                    statement.setLong(2, before);
                    statement.setLong(3, after);
                    statement.setString(4, serverUUID.toString());
                }

                @Override
                public NavigableMap<Long, Integer> processResults(ResultSet set) throws SQLException {
                    NavigableMap<Long, Integer> newPerDay = new TreeMap<>();
                    while (set.next()) {
                        newPerDay.put(set.getLong("date"), set.getInt("player_count"));
                    }
                    return newPerDay;
                }
            });
        };
    }
}