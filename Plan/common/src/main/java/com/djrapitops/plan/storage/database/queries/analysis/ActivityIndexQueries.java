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

import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.tables.ServerTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.utilities.dev.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

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
 * You can see the function plotted <a href="https://www.wolframalpha.com/input/?i=1+%2F+(x%2B1)+from+-1+to+2">here</a>
 * <p>
 * To fine tune the curve pi/2 is used since it felt like a good curve.
 * <p>
 * Activity index A is calculated by using the formula:
 * {@code A = 5 - 5 * [A(t1) + A(t2) + A(t3)] / 3}
 * <p>
 * <a href="https://www.wolframalpha.com/input/?i=plot+y+%3D+5+-+5+*+(1+%2F+(pi%2F2+*+x%2B1))+and+y+%3D1+and+y+%3D+2+and+y+%3D+3+and+y+%3D+3.75+from+-0.5+to+3">
 * Plot for A and limits
 * </a>
 * <p>
 * New Limits for A would thus be
 * {@code < 1: Inactive}
 * {@code > 1: Irregular}
 * {@code > 2: Regular}
 * {@code > 3: Active}
 * {@code > 3.75: Very Active}
 *
 * @author AuroraLS3
 */
public class ActivityIndexQueries {

    private ActivityIndexQueries() {
        // Static method class
    }

    @Benchmark.Slow("407ms")
    public static Query<Integer> fetchRegularPlayerCount(long date, ServerUUID serverUUID, long playtimeThreshold) {
        return fetchActivityGroupCount(date, serverUUID, playtimeThreshold, ActivityIndex.REGULAR, 5.1);
    }

    public static String selectActivityIndexSQL() {
        String selectActivePlaytimeSQL = SELECT +
                "ax_ux." + UserInfoTable.USER_ID + ",COALESCE(active_playtime,0) AS active_playtime" +
                FROM + UserInfoTable.TABLE_NAME + " ax_ux" +
                LEFT_JOIN + '(' + SELECT + SessionsTable.USER_ID +
                ",SUM(" + SessionsTable.SESSION_END + '-' + SessionsTable.SESSION_START + '-' + SessionsTable.AFK_TIME + ") as active_playtime" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + SessionsTable.SESSION_END + ">=?" +
                AND + SessionsTable.SESSION_START + "<=?" +
                GROUP_BY + SessionsTable.USER_ID +
                ") ax_sx on ax_sx." + SessionsTable.USER_ID + "=ax_ux." + UserInfoTable.USER_ID;

        String selectThreeWeeks = selectActivePlaytimeSQL + UNION_ALL + selectActivePlaytimeSQL + UNION_ALL + selectActivePlaytimeSQL;

        return SELECT +
                "5.0 - 5.0 * AVG(1.0 / (?/2.0 * (ax_q1.active_playtime*1.0/?) +1.0)) as activity_index," +
                "ax_u." + UsersTable.ID + " as user_id," +
                "ax_u." + UsersTable.USER_UUID +
                FROM + '(' + selectThreeWeeks + ") ax_q1" +
                INNER_JOIN + UsersTable.TABLE_NAME + " ax_u on ax_u." + UsersTable.ID + "=ax_q1." + UserInfoTable.USER_ID +
                GROUP_BY + "ax_u." + UsersTable.ID + ",ax_u." + UsersTable.USER_UUID;
    }

    public static void setSelectActivityIndexSQLParameters(PreparedStatement statement, int index, long playtimeThreshold, ServerUUID serverUUID, long date) throws SQLException {
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

    public static Query<Integer> fetchActivityGroupCount(long date, ServerUUID serverUUID, long playtimeThreshold, double above, double below) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectIndexes = SELECT + "COALESCE(activity_index, 0) as activity_index" +
                FROM + UserInfoTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectActivityIndex + ") q2 on q2." + SessionsTable.USER_ID + "=u." + UserInfoTable.USER_ID +
                WHERE + "u." + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + "u." + UserInfoTable.REGISTERED + "<=?";

        String selectCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectIndexes + ") i" +
                WHERE + "i.activity_index>=?" +
                AND + "i.activity_index<?";

        return new QueryStatement<>(selectCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, playtimeThreshold, serverUUID, date);
                statement.setString(12, serverUUID.toString());
                statement.setLong(13, date);
                statement.setDouble(14, above);
                statement.setDouble(15, below);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }

    public static Query<Map<String, Integer>> fetchActivityIndexGroupingsOn(long date, ServerUUID serverUUID, long threshold) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectIndexes = SELECT + "activity_index" +
                FROM + UserInfoTable.TABLE_NAME + " u" +
                LEFT_JOIN + '(' + selectActivityIndex + ") s on s." + SessionsTable.USER_ID + "=u." + UserInfoTable.USER_ID +
                WHERE + "u." + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + "u." + UserInfoTable.REGISTERED + "<=?";

        return new QueryStatement<>(selectIndexes) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, date);
                statement.setString(12, serverUUID.toString());
                statement.setLong(13, date);
            }

            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> groups = new HashMap<>();
                while (set.next()) {
                    double activityIndex = set.getDouble("activity_index");
                    String group = ActivityIndex.getGroup(activityIndex);
                    groups.put(group, groups.getOrDefault(group, 0) + 1);
                }
                return groups;
            }
        };
    }

    public static Query<Integer> countNewPlayersTurnedRegular(long after, long before, ServerUUID serverUUID, Long threshold) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                INNER_JOIN + UserInfoTable.TABLE_NAME + " u on u." + UserInfoTable.USER_ID + "=q2." + SessionsTable.USER_ID +
                WHERE + "u." + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                AND + "u." + UserInfoTable.REGISTERED + ">=?" +
                AND + "u." + UserInfoTable.REGISTERED + "<=?" +
                AND + "q2.activity_index>=?" +
                AND + "q2.activity_index<?";

        return new QueryStatement<>(selectActivePlayerCount) {
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
    public static Query<Integer> countRegularPlayersTurnedInactive(long start, long end, ServerUUID serverUUID, Long threshold) {
        String selectActivityIndex = selectActivityIndexSQL();

        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                // Join two select activity index queries together to query Regular and Inactive players
                INNER_JOIN + '(' + selectActivityIndex.replace("q1", "q3") + ") q4" +
                " on q2." + SessionsTable.USER_ID + "=q4." + SessionsTable.USER_ID +
                WHERE + "q2.activity_index>=?" +
                AND + "q2.activity_index<?" +
                AND + "q4.activity_index>=?" +
                AND + "q4.activity_index<?";

        return new QueryStatement<>(selectActivePlayerCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, start);
                setSelectActivityIndexSQLParameters(statement, 12, threshold, serverUUID, end);
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

    public static Query<Long> averagePlaytimePerRegularPlayer(long after, long before, ServerUUID serverUUID, Long threshold) {
        return database -> {
            // INNER JOIN limits the users to only those that are regular
            String selectPlaytimePerPlayer = SELECT +
                    "p." + SessionsTable.USER_ID + "," +
                    "SUM(p." + SessionsTable.SESSION_END + "-p." + SessionsTable.SESSION_START + ") as playtime" +
                    FROM + SessionsTable.TABLE_NAME + " p" +
                    INNER_JOIN + '(' + selectActivityIndexSQL() + ") q2 on q2." + SessionsTable.USER_ID + "=p." + SessionsTable.USER_ID +
                    WHERE + "p." + SessionsTable.SESSION_END + "<=?" +
                    AND + "p." + SessionsTable.SESSION_START + ">=?" +
                    AND + "p." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    AND + "q2.activity_index>=?" +
                    AND + "q2.activity_index<?" +
                    GROUP_BY + "p." + SessionsTable.USER_ID;
            String selectAverage = SELECT + "AVG(playtime) as average" + FROM + '(' + selectPlaytimePerPlayer + ") q1";

            return database.query(new QueryStatement<Long>(selectAverage, 100) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, before);
                    statement.setLong(12, before);
                    statement.setLong(13, after);
                    statement.setString(14, serverUUID.toString());
                    statement.setDouble(15, ActivityIndex.REGULAR);
                    statement.setDouble(16, 5.1);
                }

                @Override
                public Long processResults(ResultSet set) throws SQLException {
                    return set.next() ? (long) set.getDouble("average") : 0;
                }
            });
        };
    }

    public static Query<Long> averageSessionLengthPerRegularPlayer(long after, long before, ServerUUID serverUUID, Long threshold) {
        return database -> {
            // INNER JOIN limits the users to only those that are regular
            String selectSessionLengthPerPlayer = SELECT +
                    "p." + SessionsTable.USER_ID + "," +
                    "p." + SessionsTable.SESSION_END + "-p." + SessionsTable.SESSION_START + " as length" +
                    FROM + SessionsTable.TABLE_NAME + " p" +
                    INNER_JOIN + '(' + selectActivityIndexSQL() + ") q2 on q2." + SessionsTable.USER_ID + "=p." + SessionsTable.USER_ID +
                    WHERE + "p." + SessionsTable.SESSION_END + "<=?" +
                    AND + "p." + SessionsTable.SESSION_START + ">=?" +
                    AND + "p." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    AND + "q2.activity_index>=?" +
                    AND + "q2.activity_index<?";
            String selectAverage = SELECT + "AVG(length) as average" + FROM + '(' + selectSessionLengthPerPlayer + ") q1";

            return database.query(new QueryStatement<Long>(selectAverage, 100) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, before);
                    statement.setLong(12, before);
                    statement.setLong(13, after);
                    statement.setString(14, serverUUID.toString());
                    statement.setDouble(15, ActivityIndex.REGULAR);
                    statement.setDouble(16, 5.1);
                }

                @Override
                public Long processResults(ResultSet set) throws SQLException {
                    return set.next() ? (long) set.getDouble("average") : 0;
                }
            });
        };
    }

    public static Query<Long> averageAFKPerRegularPlayer(long after, long before, ServerUUID serverUUID, Long threshold) {
        return database -> {
            // INNER JOIN limits the users to only those that are regular
            String selectPlaytimePerPlayer = SELECT +
                    "p." + SessionsTable.USER_ID + "," +
                    "SUM(p." + SessionsTable.AFK_TIME + ") as afk" +
                    FROM + SessionsTable.TABLE_NAME + " p" +
                    INNER_JOIN + '(' + selectActivityIndexSQL() + ") q2 on q2." + SessionsTable.USER_ID + "=p." + SessionsTable.USER_ID +
                    WHERE + "p." + SessionsTable.SESSION_END + "<=?" +
                    AND + "p." + SessionsTable.SESSION_START + ">=?" +
                    AND + "p." + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID +
                    AND + "q2.activity_index>=?" +
                    AND + "q2.activity_index<?" +
                    GROUP_BY + "p." + SessionsTable.USER_ID;
            String selectAverage = SELECT + "AVG(afk) as average" + FROM + '(' + selectPlaytimePerPlayer + ") q1";

            return database.query(new QueryStatement<Long>(selectAverage, 100) {
                @Override
                public void prepare(PreparedStatement statement) throws SQLException {
                    setSelectActivityIndexSQLParameters(statement, 1, threshold, serverUUID, before);
                    statement.setLong(12, before);
                    statement.setLong(13, after);
                    statement.setString(14, serverUUID.toString());
                    statement.setDouble(15, ActivityIndex.REGULAR);
                    statement.setDouble(16, 5.1);
                }

                @Override
                public Long processResults(ResultSet set) throws SQLException {
                    return set.next() ? (long) set.getDouble("average") : 0;
                }
            });
        };
    }

    public static Query<Collection<ActivityIndex>> activityIndexForNewPlayers(long after, long before, ServerUUID serverUUID, Long threshold) {
        String selectNewUUIDs = SELECT + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        String sql = SELECT + "activity_index" +
                FROM + '(' + selectNewUUIDs + ") n" +
                INNER_JOIN + '(' + selectActivityIndexSQL() + ") a on n." + SessionsTable.USER_ID + "=a." + SessionsTable.USER_ID;

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, before);
                statement.setLong(2, after);
                statement.setString(3, serverUUID.toString());
                setSelectActivityIndexSQLParameters(statement, 4, threshold, serverUUID, before);
            }

            @Override
            public Collection<ActivityIndex> processResults(ResultSet set) throws SQLException {
                Collection<ActivityIndex> indexes = new ArrayList<>();
                while (set.next()) {
                    indexes.add(new ActivityIndex(set.getDouble("activity_index"), before));
                }
                return indexes;
            }
        };
    }

    public static Query<ActivityIndex> averageActivityIndexForRetainedPlayers(long after, long before, ServerUUID serverUUID, Long threshold) {
        String selectNewUUIDs = SELECT + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        String selectUniqueUUIDs = SELECT + "DISTINCT " + SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SESSION_END + "<=?" +
                AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        String sql = SELECT + "AVG(activity_index) as average" +
                FROM + '(' + selectNewUUIDs + ") n" +
                INNER_JOIN + '(' + selectUniqueUUIDs + ") u on n." + SessionsTable.USER_ID + "=u." + SessionsTable.USER_ID +
                INNER_JOIN + '(' + selectActivityIndexSQL() + ") a on n." + SessionsTable.USER_ID + "=a." + SessionsTable.USER_ID;

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, before);
                statement.setLong(2, after);
                statement.setString(3, serverUUID.toString());

                // Have played in the last half of the time frame
                long half = before - (before - after) / 2;
                statement.setLong(4, half);
                statement.setLong(5, before);
                statement.setString(6, serverUUID.toString());
                setSelectActivityIndexSQLParameters(statement, 7, threshold, serverUUID, before);
            }

            @Override
            public ActivityIndex processResults(ResultSet set) throws SQLException {
                return set.next() ? new ActivityIndex(set.getDouble("average"), before) : new ActivityIndex(0.0, before);
            }
        };
    }

    public static Query<ActivityIndex> averageActivityIndexForNonRetainedPlayers(long after, long before, ServerUUID serverUUID, Long threshold) {
        String selectNewUUIDs = SELECT + UserInfoTable.USER_ID +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        String selectUniqueUUIDs = SELECT + "DISTINCT " + SessionsTable.USER_ID +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SESSION_END + "<=?" +
                AND + SessionsTable.SERVER_ID + "=" + ServerTable.SELECT_SERVER_ID;

        String sql = SELECT + "AVG(activity_index) as average" +
                FROM + '(' + selectNewUUIDs + ") n" +
                LEFT_JOIN + '(' + selectUniqueUUIDs + ") u on n." + SessionsTable.USER_ID + "=u." + SessionsTable.USER_ID +
                INNER_JOIN + '(' + selectActivityIndexSQL() + ") a on n." + SessionsTable.USER_ID + "=a." + SessionsTable.USER_ID +
                WHERE + "n." + SessionsTable.USER_ID + IS_NULL;

        return new QueryStatement<>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setLong(1, before);
                statement.setLong(2, after);
                statement.setString(3, serverUUID.toString());

                // Have played in the last half of the time frame
                long half = before - (before - after) / 2;
                statement.setLong(4, half);
                statement.setLong(5, before);
                statement.setString(6, serverUUID.toString());
                setSelectActivityIndexSQLParameters(statement, 7, threshold, serverUUID, before);
            }

            @Override
            public ActivityIndex processResults(ResultSet set) throws SQLException {
                return set.next() ? new ActivityIndex(set.getDouble("average"), before) : new ActivityIndex(0.0, before);
            }
        };
    }
}