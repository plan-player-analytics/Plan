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

import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.db.access.Query;
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.UserInfoTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.db.sql.parsing.Sql.*;

/**
 * Queries for Activity Index that attempts to gain insight into player activity levels.
 * <p>
 * Old formula for activity index was not linear and difficult to turn into a query due to conditional multipliers.
 * Thus a new formula was written.
 * <p>
 * {@code T} - Time played after someone is considered active on a particular week
 * {@code t1, t2, t3} - Time played that week
 * <p>
 * Activity index takes into account last 3 weeks.
 * <p>
 * Activity for a single week is calculated using {@code A(t) = (1 / (pi/2 * (t/T) + 1))}.
 * A(t) is based on function f(x) = 1 / (x + 1), which has property f(0) = 1, decreasing from there, but not in a straight line.
 * You can see the function plotted here https://www.wolframalpha.com/input/?i=1+%2F+(x%2B1)+from+-1+to+2
 * <p>
 * To fine tune the curve pi/2 is used since it felt like a good curve.
 * <p>
 * Activity index A is calculated by using the formula:
 * {@code A = 5 - 5 * [A(t1) + A(t2) + A(t3)] / 3}
 * <p>
 * Plot for A and limits
 * https://www.wolframalpha.com/input/?i=plot+y+%3D+5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1))+and+y+%3D1+and+y+%3D+2+and+y+%3D+3+and+y+%3D+3.75+from+-0.5+to+3
 * <p>
 * New Limits for A would thus be
 * {@code < 1: Inactive}
 * {@code > 1: Irregular}
 * {@code > 2: Regular}
 * {@code > 3: Active}
 * {@code > 3.75: Very Active}
 *
 * @author Rsl1122
 */
public class ActivityIndexQueries {

    private ActivityIndexQueries() {
        // Static method class
    }

    public static Query<Integer> fetchRegularPlayerCount(long date, UUID serverUUID, long playtimeThreshold) {
        return fetchActivityGroupCount(date, serverUUID, playtimeThreshold, ActivityIndex.REGULAR, 5.1);
    }

    private static String selectActivityIndexSQL() {
        String selectActivePlaytimeSQL = SELECT +
                SessionsTable.USER_UUID +
                ",SUM(" +
                SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME +
                ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_UUID + "=?" +
                AND + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SESSION_END + "<=?" +
                GROUP_BY + SessionsTable.USER_UUID;

        String selectThreeWeeks = selectActivePlaytimeSQL + UNION + selectActivePlaytimeSQL + UNION + selectActivePlaytimeSQL;

        return SELECT +
                "5.0 - 5.0 * AVG(1 / (?/2 * (q1.active_playtime/?) +1)) as activity_index," +
                "q1." + SessionsTable.USER_UUID +
                FROM + '(' + selectThreeWeeks + ") q1" +
                GROUP_BY + "q1." + SessionsTable.USER_UUID;
    }

    private static void setSelectActivityIndexSQLParameters(PreparedStatement statement, int index, long playtimeThreshold, UUID serverUUID, long date) throws SQLException {
        statement.setDouble(index, Math.PI);
        statement.setLong(index + 1, playtimeThreshold);

        statement.setString(index + 2, serverUUID.toString());
        statement.setLong(index + 3, date - TimeUnit.DAYS.toMillis(7L));
        statement.setLong(index + 4, date);
        statement.setString(index + 5, serverUUID.toString());
        statement.setLong(index + 6, date - TimeUnit.DAYS.toMillis(14L));
        statement.setLong(index + 7, date - TimeUnit.DAYS.toMillis(7L));
        statement.setString(index + 8, serverUUID.toString());
        statement.setLong(index + 9, date - TimeUnit.DAYS.toMillis(21L));
        statement.setLong(index + 10, date - TimeUnit.DAYS.toMillis(14L));
    }

    public static Query<Integer> fetchActivityGroupCount(long date, UUID serverUUID, long playtimeThreshold, double above, double below) {
        String selectActivityIndex = selectActivityIndexSQL();

        // TODO Include users with 0 sessions in Inactive group
        // TODO Take into account player's register date
        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                WHERE + "q2.activity_index>=?" +
                AND + "q2.activity_index<?";

        return new QueryStatement<Integer>(selectActivePlayerCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, playtimeThreshold, serverUUID, date);
                statement.setDouble(12, above);
                statement.setDouble(13, below);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }

    public static Query<Map<String, Integer>> fetchActivityIndexGroupingsOn(long date, UUID serverUUID, long threshold) {
        return db -> {
            Map<String, Integer> groups = new HashMap<>();
            groups.put("Very Active", db.query(fetchActivityGroupCount(date, serverUUID, threshold, ActivityIndex.VERY_ACTIVE, 5.1)));
            groups.put("Active", db.query(fetchActivityGroupCount(date, serverUUID, threshold, ActivityIndex.ACTIVE, ActivityIndex.VERY_ACTIVE)));
            groups.put("Regular", db.query(fetchActivityGroupCount(date, serverUUID, threshold, ActivityIndex.REGULAR, ActivityIndex.ACTIVE)));
            groups.put("Irregular", db.query(fetchActivityGroupCount(date, serverUUID, threshold, ActivityIndex.IRREGULAR, ActivityIndex.REGULAR)));
            groups.put("Inactive", db.query(fetchActivityGroupCount(date, serverUUID, threshold, -0.1, ActivityIndex.IRREGULAR)));
            return groups;
        };
    }

    public static Query<Integer> countNewPlayersTurnedRegular(long after, long before, UUID serverUUID, Long threshold) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " u on u." + UserInfoTable.USER_UUID + "=q2." + SessionsTable.USER_UUID +
                WHERE + "u." + UserInfoTable.SERVER_UUID + "=?" +
                AND + "u." + UserInfoTable.REGISTERED + ">=?" +
                AND + "u." + UserInfoTable.REGISTERED + "<=?" +
                AND + "q2.activity_index>=?" +
                AND + "q2.activity_index<?";

        return new QueryStatement<Integer>(selectActivePlayerCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, before);
                statement.setString(12, serverUUID.toString());
                statement.setLong(13, after);
                statement.setLong(14, before);
                statement.setDouble(15, ActivityIndex.REGULAR);
                statement.setDouble(16, 5.1);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }

    /**
     * @param start      Start of the tracking, those regular will be counted here.
     * @param end        End of the tracking, those inactive will be count here.
     * @param serverUUID UUID of the server.
     * @param threshold  Playtime threshold
     * @return Query how many players went from regular to inactive in a span of time.
     */
    public static Query<Integer> countRegularPlayersTurnedInactive(long start, long end, UUID serverUUID, Long threshold) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                // Join two select activity index queries together to query Regular and Inactive players
                INNER_JOIN + '(' + selectActivityIndex.replace("q1", "q3") + ") q4" +
                " on q2." + SessionsTable.USER_UUID + "=q4." + SessionsTable.USER_UUID +
                WHERE + "q2.activity_index>=?" +
                AND + "q2.activity_index<?" +
                AND + "q4.activity_index>=?" +
                AND + "q4.activity_index<?";

        return new QueryStatement<Integer>(selectActivePlayerCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, end);
                setSelectActivityIndexSQLParameters(statement, 12, threshold, serverUUID, start);
                statement.setDouble(23, ActivityIndex.REGULAR);
                statement.setDouble(24, 5.1);
                statement.setDouble(25, -0.1);
                statement.setDouble(26, ActivityIndex.IRREGULAR);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }
}