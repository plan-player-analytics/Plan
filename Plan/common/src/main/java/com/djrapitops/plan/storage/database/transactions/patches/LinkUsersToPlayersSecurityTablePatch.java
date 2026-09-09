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

import com.djrapitops.plan.storage.database.queries.HasMoreThanZeroQueryStatement;
import com.djrapitops.plan.storage.database.queries.QueryAllStatement;
import com.djrapitops.plan.storage.database.sql.building.Sql;
import com.djrapitops.plan.storage.database.sql.tables.UsersTable;
import com.djrapitops.plan.storage.database.sql.tables.webuser.SecurityTable;
import com.djrapitops.plan.storage.database.transactions.ExecBatchStatement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static com.djrapitops.plan.storage.database.sql.building.Sql.*;

/**
 * Populates new linked_to_uuid field with the uuid of a username (same as minecraft name) or 'console'.
 *
 * @author AuroraLS3
 * @see LinkedToSecurityTablePatch for addition of the field.
 */
public class LinkUsersToPlayersSecurityTablePatch extends Patch {

    @Override
    public boolean hasBeenApplied() {
        String sql = SELECT + "COUNT(1) as c" + FROM + SecurityTable.TABLE_NAME +
                WHERE + SecurityTable.LINKED_TO + "=''" + lockForUpdate();
        return !query(new HasMoreThanZeroQueryStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) {
                // No preparation necessary
            }
        });
    }

    @Override
    protected void applyPatch() {
        String querySQL = SELECT + UsersTable.USER_UUID + ',' + SecurityTable.USERNAME +
                FROM + SecurityTable.TABLE_NAME +
                LEFT_JOIN + UsersTable.TABLE_NAME + " on " + UsersTable.USER_NAME + "=" + SecurityTable.USERNAME +
                WHERE + SecurityTable.LINKED_TO + "=''" + lockForUpdate();
        String sql = "UPDATE " + SecurityTable.TABLE_NAME + " SET " + SecurityTable.LINKED_TO + "=?" +
                WHERE + SecurityTable.USERNAME + "=?";

        Map<String, String> byUsername = query(new QueryAllStatement<>(querySQL) {
            @Override
            public Map<String, String> processResults(ResultSet set) throws SQLException {
                Map<String, String> byUsername = new HashMap<>();
                while (set.next()) {
                    byUsername.put(set.getString(SecurityTable.USERNAME), set.getString(UsersTable.USER_UUID));
                }
                return byUsername;
            }
        });
        execute(new ExecBatchStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                for (Map.Entry<String, String> usernameUUIDPair : byUsername.entrySet()) {
                    Sql.setStringOrNull(statement, 1, usernameUUIDPair.getValue());
                    statement.setString(2, usernameUUIDPair.getKey());
                    statement.addBatch();
                }
            }
        });
    }
}
