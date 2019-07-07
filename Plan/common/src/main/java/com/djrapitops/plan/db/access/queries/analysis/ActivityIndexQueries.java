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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Query<Integer> fetchRegularPlayerCount(long date, UUID serverUUID, long playtimeThreshold) {
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

        String selectActivityIndex = SELECT +
                "5.0 - 5.0 * AVG(1 / (?/2 * (q1.active_playtime/?) +1)) as activity_index," +
                "q1." + SessionsTable.USER_UUID +
                FROM + '(' + selectThreeWeeks + ") q1" +
                GROUP_BY + "q1." + SessionsTable.USER_UUID;

        String selectActivePlayerCount = SELECT + "COUNT(1) as count" +
                FROM + '(' + selectActivityIndex + ") q2" +
                WHERE + "q2.activity_index>=?";

        return new QueryStatement<Integer>(selectActivePlayerCount) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setDouble(1, Math.PI);
                statement.setLong(2, playtimeThreshold);

                statement.setString(3, serverUUID.toString());
                statement.setLong(4, date - TimeUnit.DAYS.toMillis(7L));
                statement.setLong(5, date);
                statement.setString(6, serverUUID.toString());
                statement.setLong(7, date - TimeUnit.DAYS.toMillis(14L));
                statement.setLong(8, date - TimeUnit.DAYS.toMillis(7L));
                statement.setString(9, serverUUID.toString());
                statement.setLong(10, date - TimeUnit.DAYS.toMillis(21L));
                statement.setLong(11, date - TimeUnit.DAYS.toMillis(14L));

                statement.setDouble(12, ActivityIndex.REGULAR);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt("count") : 0;
            }
        };
    }

}