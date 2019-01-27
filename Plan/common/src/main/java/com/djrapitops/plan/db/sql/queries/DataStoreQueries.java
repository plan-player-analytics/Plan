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
package com.djrapitops.plan.db.sql.queries;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.Executable;
import com.djrapitops.plan.db.sql.tables.CommandUseTable;
import com.djrapitops.plan.db.sql.tables.ServerTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Static method class for single item store queries.
 *
 * @author Rsl1122
 */
public class DataStoreQueries {

    private DataStoreQueries() {
        /* static method class */
    }

    public static Executable storeUsedCommandInformation(UUID serverUUID, String commandName) {
        return connection -> {
            if (!updateCommandUsage(serverUUID, commandName).execute(connection)) {
                insertNewCommandUsage(serverUUID, commandName).execute(connection);
            }
            return false;
        };
    }

    private static Executable updateCommandUsage(UUID serverUUID, String commandName) {
        String sql = "UPDATE " + CommandUseTable.TABLE_NAME + " SET "
                + CommandUseTable.TIMES_USED + "=" + CommandUseTable.TIMES_USED + "+ 1" +
                " WHERE " + CommandUseTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + CommandUseTable.COMMAND + "=?";

        return new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, commandName);
            }
        };
    }

    private static Executable insertNewCommandUsage(UUID serverUUID, String commandName) {
        return new ExecStatement(CommandUseTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, commandName);
                statement.setInt(2, 1);
                statement.setString(3, serverUUID.toString());
            }
        };
    }

}