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
package com.djrapitops.plan.storage.database.queries.analysis;

import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for server overview tab data.
 *
 * @author AuroraLS3
 */
public class PlayerCountQueries {

    private static final String PLAYER_COUNT = "player_count";

    private PlayerCountQueries() {
        // Static method class
    }

    public static Query<Integer> uniquePlayerCount(long after, long before, ServerUUID serverUUID) {
        return database -> database.queryOptional(SELECT + "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                                FROM + SessionsTable.TABLE_NAME +
                                WHERE + SessionsTable.SESSION_END + "<=?" +
                                AND + SessionsTable.SESSION_START + ">=?" +
                                AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID,
                        set -> set.getInt(PLAYER_COUNT),
                        before, after, serverUUID)
                .orElse(0);
    }

    /**
     * Fetch uniquePlayer count for ALL servers.
     *
     * @param after  After epoch ms
     * @param before Before epoch ms
     * @return Unique player count (players who played within time frame)
     */
    public static Query<Integer> uniquePlayerCount(long after, long before) {
        return database -> database.queryOptional(SELECT + "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                                FROM + SessionsTable.TABLE_NAME +
                                WHERE + SessionsTable.SESSION_END + "<=?" +
                                AND + SessionsTable.SESSION_START + ">=?",
                        set -> set.getInt(PLAYER_COUNT),
                        before, after)
                .orElse(0);
    }

    public static Query<Map<ServerUUID, Integer>> uniquePlayerCounts(long after, long before) {
        String sql = SELECT + ServerTable.SERVER_UUID + ",COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                FROM + SessionsTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " se on se." + ServerTable.ID + "=" + SessionsTable.TABLE_NAME + '.' + SessionsTable.SERVER_ID +
                WHERE + SessionsTable.SESSION_END + "<=?" +
                AND + SessionsTable.SESSION_START + ">=?" +
                GROUP_BY + ServerTable.SERVER_UUID;

        return database -> database.queryMap(sql,
                (set, byServer) -> byServer.put(
                        ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                        set.getInt(PLAYER_COUNT)
                ),
                before, after);
    }

    /**
     * Fetch a EpochMs - Count map of unique players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many unique players played that day
     */
    public static Query<NavigableMap<Long, Integer>> uniquePlayerCounts(long after, long before, long timeZoneOffset, ServerUUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";

            return database.queryMap(selectUniquePlayersPerDay,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after, serverUUID);
        };
    }

    /**
     * Fetch a EpochMs - Count map of unique players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many unique players played that day
     */
    public static Query<NavigableMap<Long, Integer>> hourlyUniquePlayerCounts(long after, long before, long timeZoneOffset, ServerUUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToHourStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";

            return database.queryMap(selectUniquePlayersPerDay,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after, serverUUID);
        };
    }

    /**
     * Fetch a EpochMs - Count map of unique players on ALL servers.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many unique players played that day
     */
    public static Query<NavigableMap<Long, Integer>> uniquePlayerCounts(long after, long before, long timeZoneOffset) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    GROUP_BY + "date";

            return database.queryMap(selectUniquePlayersPerDay,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after);
        };
    }

    /**
     * Fetch a EpochMs - Count map of unique players on ALL servers.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many unique players played that day
     */
    public static Query<NavigableMap<Long, Integer>> hourlyUniquePlayerCounts(long after, long before, long timeZoneOffset) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToHourStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    GROUP_BY + "date";

            return database.queryMap(selectUniquePlayersPerDay,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after);
        };
    }

    public static Query<Integer> averageUniquePlayerCount(long after, long before, long timeZoneOffset, ServerUUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";
            String selectAverage = SELECT + "AVG(" + PLAYER_COUNT + ") as average" + FROM + '(' + selectUniquePlayersPerDay + ") q1";

            return database.queryOptional(selectAverage,
                            set -> (int) set.getDouble("average"),
                            timeZoneOffset, before, after, serverUUID)
                    .orElse(0);
        };
    }

    public static Query<Integer> averageUniquePlayerCount(long after, long before, long timeZoneOffset) {
        return database -> {
            Sql sql = database.getSql();
            String selectUniquePlayersPerDay = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + SessionsTable.SESSION_START + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(DISTINCT " + SessionsTable.USER_ID + ") as " + PLAYER_COUNT +
                    FROM + SessionsTable.TABLE_NAME +
                    WHERE + SessionsTable.SESSION_END + "<=?" +
                    AND + SessionsTable.SESSION_START + ">=?" +
                    GROUP_BY + "date";
            String selectAverage = SELECT + "AVG(" + PLAYER_COUNT + ") as average" + FROM + '(' + selectUniquePlayersPerDay + ") q1";

            return database.queryOptional(selectAverage,
                            set -> (int) set.getDouble("average"),
                            timeZoneOffset, before, after)
                    .orElse(0);
        };
    }

    public static Query<Integer> newPlayerCount(long after, long before, ServerUUID serverUUID) {
        String sql = SELECT + "COUNT(1) as " + PLAYER_COUNT +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        return database -> database.queryOptional(sql,
                        set -> set.getInt(PLAYER_COUNT),
                        before, after, serverUUID)
                .orElse(0);
    }

    public static Query<Integer> newPlayerCount(long after, long before) {
        String sql = SELECT + "COUNT(1) as " + PLAYER_COUNT +
                FROM + UsersTable.TABLE_NAME +
                WHERE + UsersTable.REGISTERED + "<=?" +
                AND + UsersTable.REGISTERED + ">=?";

        return database -> database.queryOptional(sql,
                        set -> set.getInt(PLAYER_COUNT),
                        before, after)
                .orElse(0);
    }

    public static Query<Map<ServerUUID, Integer>> newPlayerCounts(long after, long before) {
        String sql = SELECT + "s." + ServerTable.SERVER_UUID + ",COUNT(1) as " + PLAYER_COUNT +
                FROM + UserInfoTable.TABLE_NAME +
                INNER_JOIN + ServerTable.TABLE_NAME + " s on s." + ServerTable.ID + '=' + UserInfoTable.TABLE_NAME + '.' + UserInfoTable.SERVER_ID +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                GROUP_BY + "s." + ServerTable.SERVER_UUID;

        return database -> database.queryMap(sql,
                (set, byServer) -> byServer.put(
                        ServerUUID.fromString(set.getString(ServerTable.SERVER_UUID)),
                        set.getInt(PLAYER_COUNT)
                ),
                before, after);
    }

    /**
     * Fetch a EpochMs - Count map of new players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many new players joined that day
     */
    public static Query<NavigableMap<Long, Integer>> newPlayerCounts(long after, long before, long timeZoneOffset, ServerUUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as " + PLAYER_COUNT +
                    FROM + UserInfoTable.TABLE_NAME +
                    WHERE + UserInfoTable.REGISTERED + "<=?" +
                    AND + UserInfoTable.REGISTERED + ">=?" +
                    AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";

            return database.queryMap(selectNewPlayersQuery,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after, serverUUID);
        };
    }

    /**
     * Fetch a EpochMs - Count map of new players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @param serverUUID     UUID of the Plan server
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many new players joined that day
     */
    public static Query<NavigableMap<Long, Integer>> hourlyNewPlayerCounts(long after, long before, long timeZoneOffset, UUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToHourStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as " + PLAYER_COUNT +
                    FROM + UserInfoTable.TABLE_NAME +
                    WHERE + UserInfoTable.REGISTERED + "<=?" +
                    AND + UserInfoTable.REGISTERED + ">=?" +
                    AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";

            return database.queryMap(selectNewPlayersQuery,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after, serverUUID);
        };
    }

    /**
     * Fetch a EpochMs - Count map of new players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many new players joined that day
     */
    public static Query<NavigableMap<Long, Integer>> newPlayerCounts(long after, long before, long timeZoneOffset) {
        return database -> {
            Sql sql = database.getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as " + PLAYER_COUNT +
                    FROM + UsersTable.TABLE_NAME +
                    WHERE + UsersTable.REGISTERED + "<=?" +
                    AND + UsersTable.REGISTERED + ">=?" +
                    GROUP_BY + "date";

            return database.queryMap(selectNewPlayersQuery,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after);
        };
    }

    /**
     * Fetch a EpochMs - Count map of new players on a server.
     *
     * @param after          After epoch ms
     * @param before         Before epoch ms
     * @param timeZoneOffset Offset from {@link java.util.TimeZone#getOffset(long)}, applied to the dates before grouping.
     * @return Map: Epoch ms (Start of day at 0 AM, no offset) - How many new players joined that day
     */
    public static Query<NavigableMap<Long, Integer>> hourlyNewPlayerCounts(long after, long before, long timeZoneOffset) {
        return database -> {
            Sql sql = database.getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToHourStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as " + PLAYER_COUNT +
                    FROM + UsersTable.TABLE_NAME +
                    WHERE + UsersTable.REGISTERED + "<=?" +
                    AND + UsersTable.REGISTERED + ">=?" +
                    GROUP_BY + "date";

            return database.queryMap(selectNewPlayersQuery,
                    (set, perDay) -> perDay.put(set.getLong("date"), set.getInt(PLAYER_COUNT)),
                    TreeMap::new,
                    timeZoneOffset, before, after);
        };
    }

    public static Query<Integer> averageNewPlayerCount(long after, long before, long timeZoneOffset, ServerUUID serverUUID) {
        return database -> {
            Sql sql = database.getSql();
            String selectNewPlayersQuery = SELECT +
                    sql.dateToEpochSecond(sql.dateToDayStamp(sql.epochSecondToDate('(' + UserInfoTable.REGISTERED + "+?)/1000"))) +
                    "*1000 as date," +
                    "COUNT(1) as " + PLAYER_COUNT +
                    FROM + UserInfoTable.TABLE_NAME +
                    WHERE + UserInfoTable.REGISTERED + "<=?" +
                    AND + UserInfoTable.REGISTERED + ">=?" +
                    AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    GROUP_BY + "date";
            String selectAverage = SELECT + "AVG(" + PLAYER_COUNT + ") as average" + FROM + '(' + selectNewPlayersQuery + ") q1";

            return database.queryOptional(selectAverage,
                            set -> (int) set.getDouble("average"),
                            timeZoneOffset, before, after, serverUUID)
                    .orElse(0);
        };
    }

    public static Query<Integer> retainedPlayerCount(long after, long before, ServerUUID serverUUID) {
        String selectUniqueUUIDs = SELECT + DISTINCT + "s." + SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME + " s" +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " ux" +
                " on ux." + UserInfoTable.USER_ID + "=s." + SessionsTable.USER_ID +
                AND + "ux." + UserInfoTable.SERVER_ID + "=s." + SessionsTable.SERVER_ID +
                WHERE + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.REGISTERED + "<=?" +
                AND + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SESSION_END + "<=?" +
                AND + "s." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        return new QueryStatement<>(selectUniqueUUIDs) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, after);
                statement.setLong(2, before);

                // Have played in the last half of the time frame
                long half = before - (before - after) / 2;
                statement.setLong(3, half);
                statement.setLong(4, before);
                statement.setString(5, serverUUID.toString());
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                int count = 0;
                while (set.next()) {
                    count++;
                }
                return count;
            }
        };
    }

    public static Query<Integer> operators(ServerUUID serverUUID) {
        String sql = SELECT + "COUNT(1) as " + PLAYER_COUNT + FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + UserInfoTable.OP + "=?";
        return db -> db.queryOptional(sql, set -> set.getInt(PLAYER_COUNT), serverUUID, true)
                .orElse(0);
    }
}