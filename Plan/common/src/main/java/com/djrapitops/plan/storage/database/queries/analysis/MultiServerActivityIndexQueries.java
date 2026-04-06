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
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import org.jspecify.annotations.NonNull;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
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
        if (serverUUIDs.isEmpty()) {
            return NetworkActivityIndexQueries.fetchActivityGroupCount(date, playtimeThreshold, above, below);
        }

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

    private static String selectActivityIndexOfPlayersOnServer(long date, long playtimeThreshold, Collection<ServerUUID> serverUUIDs) {
        long weekAgo = date - TimeUnit.DAYS.toMillis(7L);
        long twoWeeksAgo = date - TimeUnit.DAYS.toMillis(14L);
        long threeWeeksAgo = date - TimeUnit.DAYS.toMillis(21L);
        String selectUserIds = SELECT + DISTINCT + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs);

        String selectWeeks = SELECT +
                "ps.user_id, " +
                week(weekAgo, date, "w1") + ',' +
                week(twoWeeksAgo, weekAgo, "w2") + ',' +
                week(threeWeeksAgo, twoWeeksAgo, "w3") +
                FROM + SessionsTable.TABLE_NAME + " ps " +
                WHERE + "ps." + SessionsTable.SERVER_ID + " IN " + ServerTable.selectServerIds(serverUUIDs) +
                AND + "ps." + SessionsTable.SESSION_END + ">=" + threeWeeksAgo + " " +
                AND + "ps." + SessionsTable.SESSION_START + "<" + date + " " +
                GROUP_BY + "ps." + SessionsTable.USER_ID;

        return SELECT +
                "u." + UserInfoTable.USER_ID + ", " +
                "5.0 - 5.0 * ( " +
                "( " +
                "1 / (PI()/2 * (COALESCE(s.w1,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w2,0) / " + playtimeThreshold + ") + 1) + " +
                "1 / (PI()/2 * (COALESCE(s.w3,0) / " + playtimeThreshold + ") + 1) " +
                ") / 3 " +
                ") AS activity_index " +
                FROM + "(" + selectUserIds + ") u " +
                LEFT_JOIN + "( " + selectWeeks + ") s ON s." + SessionsTable.USER_ID + "=u." + UserInfoTable.USER_ID;
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
