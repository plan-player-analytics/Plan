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
package com.djrapitops.plan.storage.database.transactions.patches;

import com.djrapitops.plan.storage.database.queries.Query;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.queries.QueryStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.JoinAddressTable;
import com.djrapitops.plan.storage.database.sql.tables.SessionsTable;
import com.djrapitops.plan.storage.database.sql.tables.UserInfoTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;
import org.apache.commons.lang3.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Adds join_address_id to plan_sessions, and populates latest session rows with value from plan_user_info.
 *
 * @author AuroraLS3
 */
public class SessionJoinAddressPatch extends Patch {

    public static Query<List<String>> uniqueJoinAddressesOld() {
        String sql = SELECT + DISTINCT + "COALESCE(" + UserInfoTable.JOIN_ADDRESS + ", ?) as address" +
                FROM + UserInfoTable.TABLE_NAME +
                ORDER_BY + "address ASC";
        return new QueryStatement<>(sql, 100) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            }

            @Override
            public List<String> processResults(ResultSet set) throws SQLException {
                List<String> joinAddresses = new ArrayList<>();
                while (set.next()) joinAddresses.add(set.getString("address"));
                return joinAddresses;
            }
        };
    }

    @Override
    public boolean hasBeenApplied() {
        return hasColumn(SessionsTable.TABLE_NAME, SessionsTable.JOIN_ADDRESS_ID);
    }

    @Override
    protected void applyPatch() {
        Integer defaultAddressId = getDefaultAddressId();
        addColumn(SessionsTable.TABLE_NAME, SessionsTable.JOIN_ADDRESS_ID + ' ' + Sql.INT + " DEFAULT " + defaultAddressId);

        populateAddresses();
        populateLatestSessions();
    }

    private Integer getDefaultAddressId() {
        return query(new QueryStatement<>(SELECT + ID +
                FROM + JoinAddressTable.TABLE_NAME +
                WHERE + JoinAddressTable.JOIN_ADDRESS + "=?") {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
            }

            @Override
            public Integer processResults(ResultSet set) throws SQLException {
                return set.next() ? set.getInt(JoinAddressTable.ID) : 1;
            }
        });
    }

    private void populateAddresses() {
        List<String> joinAddresses = query(uniqueJoinAddressesOld());
        joinAddresses.remove(JoinAddressTable.DEFAULT_VALUE_FOR_LOOKUP);
        execute(new ExecBatchStatement(JoinAddressTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (String joinAddress : joinAddresses) {
                    statement.setString(1, StringUtils.truncate(joinAddress, JoinAddressTable.JOIN_ADDRESS_MAX_LENGTH));
                    statement.addBatch();
                }
            }
        });
    }

    private void populateLatestSessions() {
        String selectLatestSessionIds = SELECT +
                "MAX(s." + SessionsTable.ID + ") as session_id," +
                "u." + UserInfoTable.USER_ID + ',' +
                "u." + UserInfoTable.SERVER_ID + ',' +
                "u." + UserInfoTable.JOIN_ADDRESS +
                FROM + UserInfoTable.TABLE_NAME + " u" +
                INNER_JOIN + SessionsTable.TABLE_NAME + " s on s." + SessionsTable.USER_ID + "=u." + UserInfoTable.USER_ID +
                AND + "s." + SessionsTable.SERVER_ID + "=u." + UserInfoTable.SERVER_ID +
                GROUP_BY + "u." + UserInfoTable.USER_ID + ',' + "u." + UserInfoTable.SERVER_ID + ',' + "u." + UserInfoTable.JOIN_ADDRESS;

        String sql = SELECT +
                "session_id," +
                "j." + JoinAddressTable.ID + " as join_address_id" +
                FROM + '(' + selectLatestSessionIds + ") q1 " +
                INNER_JOIN + JoinAddressTable.TABLE_NAME + " j on " +
                "j." + JoinAddressTable.JOIN_ADDRESS + "=q1." + UserInfoTable.JOIN_ADDRESS;

        Map<Integer, Integer> joinAddressIdsBySessionId = query(new QueryAllStatement<>(sql) {
            @Override
            public Map<Integer, Integer> processResults(ResultSet set) throws SQLException {
                Map<Integer, Integer> joinAddressBySessionId = new TreeMap<>();
                while (set.next()) {
                    joinAddressBySessionId.put(
                            set.getInt("session_id"),
                            set.getInt("join_address_id")
                    );
                }
                return joinAddressBySessionId;
            }
        });

        String updateSql = "UPDATE " + SessionsTable.TABLE_NAME + " SET " + SessionsTable.JOIN_ADDRESS_ID + "=?" +
                WHERE + SessionsTable.ID + "=?";

        execute(new ExecBatchStatement(updateSql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<Integer, Integer> sessionIdAndJoinAddressId : joinAddressIdsBySessionId.entrySet()) {
                    Integer sessionId = sessionIdAndJoinAddressId.getKey();
                    Integer joinAddressId = sessionIdAndJoinAddressId.getValue();
                    statement.setInt(1, joinAddressId);
                    statement.setInt(2, sessionId);
                    statement.addBatch();
                }
            }
        });
    }

}
