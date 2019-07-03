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
        String sql = SELECT + "COUNT(" + SessionsTable.USER_UUID + ") as player_count" +
                FROM + SessionsTable.TABLE_NAME +
                WHERE + SessionsTable.SESSION_END + "<=?" +
                AND + SessionsTable.SESSION_START + ">=?" +
                AND + SessionsTable.SERVER_UUID + "=?" +
                GROUP_BY + SessionsTable.USER_UUID;

        return queryPlayerCount(sql, after, before, serverUUID);
    }

    public static Query<Integer> uniquePlayerCountPerDay(long after, long before, UUID serverUUID) {
        return db -> 0; // TODO
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