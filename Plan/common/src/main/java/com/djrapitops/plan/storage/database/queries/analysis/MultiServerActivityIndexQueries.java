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
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import org.jspecify.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Queries for Activity Index that support multiple ServerUUIDs.
 *
 * @author AuroraLS3
 */
public class MultiServerActivityIndexQueries {

    private MultiServerActivityIndexQueries() {
        // Static method class
    }

    public static Query<Integer> fetchActivityGroupCount(long date, Collection<ServerUUID> serverUUIDs, long playtimeThreshold, double above, double below) {
        String selectActivityIndex = selectActivityIndexOfPlayersOnServer(date, playtimeThreshold, serverUUIDs);

        String selectCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") i" +
                WHERE + "i.activity_index>=?" +
                AND + "i.activity_index<?";

        return new QueryStatement<>(selectCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setDouble(1, above);
                statement.setDouble(2, below);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }

    public static Query<Map<String, Integer>> fetchActivityGroupCounts(long date, Collection<ServerUUID> serverUUIDs, long playtimeThreshold) {
        String selectCounts = "WITH activity_indexes AS (" +
                selectActivityIndexOfPlayersOnServer(date, playtimeThreshold, serverUUIDs) +
                ")," +
                "classified AS (" +
                SELECT + " CASE WHEN activity_index < 1.0 THEN 'Inactive'\n" +
                " WHEN activity_index < 2.0 THEN 'Irregular'" +
                " WHEN activity_index < 3.0 THEN 'Regular'" +
                " WHEN activity_index < 3.75 THEN 'Active'" +
                " ELSE 'Very Active'" +
                " END AS activity_group" +
                FROM + "activity_indexes" +
                ")" +
                SELECT + "activity_group,COUNT(*) as count" + FROM + "classified" + GROUP_BY + "activity_group";

        return new QueryAllStatement<>(selectCounts) {
            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> groups = new HashMap<>();
                while (set.next()) {
                    String group = set.getString("activity_group");
                    int count = set.getInt("count");
                    groups.put(group, count);
                }
                return groups;
            }
        };
    }

    private static String selectActivityIndexOfPlayersOnServer(long date, long playtimeThreshold, Collection<ServerUUID> serverUUIDs) {
        long weekAgo = date - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = date - TimeUnit.DAYS.toMillis(14L);
        long threeWeeksAgo = date - TimeUnit.DAYS.toMillis(21L);

        String selectWeeks = SELECT +
                "ps.user_id, " +
                week(weekAgo, date, "w1") + ',' +
                week(twoWeeksAgo, weekAgo, "w2") + ',' +
                week(threeWeeksAgo, twoWeeksAgo, "w3") +
                FROM + SessionsTable.TABLE_NAME + " ps " +
                WHERE + (serverUUIDs.isEmpty()
                ? ""
                : "ps." + SessionsTable.SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                  AND) + "ps." + SessionsTable.SESSION_END + ">=" + threeWeeksAgo + " " +
                AND + "ps." + SessionsTable.SESSION_START + "<" + date + " " +
                GROUP_BY + "ps." + SessionsTable.USER_ID;

        String activityIndex = "5.0 - 5.0 * ( " +
                "( " +
                "1 / (PI()/2 * (COALESCE(s.w1,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w2,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w3,0) / " + playtimeThreshold + ") + 1) " +
                ") / 3 " +
                ") AS activity_index ";

        if (serverUUIDs.isEmpty()) {
            return SELECT + "u." + UsersTable.ID + " as user_id, " +
                    activityIndex +
                    FROM + UsersTable.TABLE_NAME + " u " +
                    LEFT_JOIN + "( " + selectWeeks + ") s ON s." + SessionsTable.USER_ID + "=u." + UsersTable.ID +
                    WHERE + UsersTable.REGISTERED + "<=" + date;
        } else {
            String selectUserIds = SELECT + UserInfoTable.USER_ID + ',' + "MIN(" + UserInfoTable.REGISTERED + ") as registered" +
                    FROM + UserInfoTable.TABLE_NAME +
                    WHERE + UserInfoTable.SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                    GROUP_BY + UserInfoTable.USER_ID;
            return SELECT +
                    "u." + UserInfoTable.USER_ID + ", " +
                    activityIndex +
                    FROM + "(" + selectUserIds + ") u " +
                    LEFT_JOIN + "( " + selectWeeks + ") s ON s." + SessionsTable.USER_ID + "=u." + UserInfoTable.USER_ID +
                    WHERE + UserInfoTable.REGISTERED + "<=" + date;
        }
    }

    private static String selectActivityIndexOfPlayerOnServers(long date, long playtimeThreshold, Collection<ServerUUID> serverUUIDs) {
        long weekAgo = date - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = date - TimeUnit.DAYS.toMillis(14L);
        long threeWeeksAgo = date - TimeUnit.DAYS.toMillis(21L);

        String selectWeeks = SELECT +
                "ps.user_id, " +
                week(weekAgo, date, "w1") + ',' +
                week(twoWeeksAgo, weekAgo, "w2") + ',' +
                week(threeWeeksAgo, twoWeeksAgo, "w3") +
                FROM + SessionsTable.TABLE_NAME + " ps " +
                WHERE + (serverUUIDs.isEmpty() ? "" : "ps." + SessionsTable.SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                                                      AND) + "ps." + SessionsTable.USER_ID + "=" + UsersTable.SELECT_USER_ID +
                AND + "ps." + SessionsTable.SESSION_END + ">=" + threeWeeksAgo + " " +
                AND + "ps." + SessionsTable.SESSION_START + "<" + date + " " +
                GROUP_BY + SessionsTable.USER_ID;

        return SELECT +
                "s." + SessionsTable.USER_ID + ", " +
                "5.0 - 5.0 * ( " +
                "( " +
                "1 / (PI()/2 * (COALESCE(s.w1,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w2,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w3,0) / " + playtimeThreshold + ") + 1) " +
                ") / 3 " +
                ") AS activity_index " +
                FROM + "( " + selectWeeks + ") s";
    }

    public static Query<Double> getActivityIndex(long date, long playtimeThreshold, UUID playerUUID, List<ServerUUID> serverUUIDs) {
        String sql = selectActivityIndexOfPlayerOnServers(date, playtimeThreshold, serverUUIDs);
        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, playerUUID.toString());
            }

            @Override
            public Double processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getDouble("activity_index") : 0.0;
            }
        };
    }

    private static @NonNull String week(long weekAgo, long date, String as) {
        return "SUM( " +
                "CASE " +
                "WHEN ps.session_end >= " + weekAgo + " " +
                "AND ps.session_start < " + date + " " +
                "THEN (ps.session_end - ps.session_start - ps.afk_time) * 1.0 " +
                "ELSE 0 " +
                "END " +
                ") AS " + as;
    }
}
