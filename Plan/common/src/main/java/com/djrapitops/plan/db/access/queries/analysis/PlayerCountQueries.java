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
import com.djrapitops.plan.db.sql.tables.SessionsTable;
import com.djrapitops.plan.db.sql.tables.UserInfoTable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    public static Query<Integer> newPlayerCount(long after, long before, UUID serverUUID) {
        String sql = SELECT + "COUNT(" + UserInfoTable.USER_UUID + ") as player_count" +
                FROM + UserInfoTable.TABLE_NAME +
                WHERE + UserInfoTable.REGISTERED + "<=?" +
                AND + UserInfoTable.REGISTERED + ">=?" +
                AND + UserInfoTable.SERVER_UUID + "=?";

        return queryPlayerCount(sql, after, before, serverUUID);
    }
}