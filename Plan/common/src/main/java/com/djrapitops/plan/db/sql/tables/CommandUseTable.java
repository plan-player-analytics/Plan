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
package com.djrapitops.plan.db.sql.tables;

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.SQLDB;
import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Table that is in charge of storing command data.
 * <p>
 * Table Name: plan_commandusages
 *
 * @author Rsl1122
 */
public class CommandUseTable extends Table {

    public static final String TABLE_NAME = "plan_commandusages";

    public static final String COMMAND_ID = "id";
    public static final String SERVER_ID = "server_id";
    public static final String COMMAND = "command";
    public static final String TIMES_USED = "times_used";

    public static final String INSERT_STATEMENT = "INSERT INTO " + TABLE_NAME + " ("
            + COMMAND + ", "
            + TIMES_USED + ", "
            + SERVER_ID
            + ") VALUES (?, ?, " + ServerTable.STATEMENT_SELECT_SERVER_ID + ")";

    public CommandUseTable(SQLDB db) {
        super(TABLE_NAME, db);
    }

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(COMMAND_ID, Sql.INT).primaryKey()
                .column(COMMAND, Sql.varchar(20)).notNull()
                .column(TIMES_USED, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.SERVER_ID)
                .toString();
    }

    public void commandUsed(String command) {
        if (command.length() > 20) {
            return;
        }

        String sql = "UPDATE " + TABLE_NAME + " SET "
                + TIMES_USED + "=" + TIMES_USED + "+ 1" +
                " WHERE " + SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + COMMAND + "=?";

        boolean updated = execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, getServerUUID().toString());
                statement.setString(2, command);
            }
        });
        if (!updated) {
            insertCommand(command);
        }
    }

    private void insertCommand(String command) {
        execute(new ExecStatement(INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, command);
                statement.setInt(2, 1);
                statement.setString(3, getServerUUID().toString());
            }
        });
    }
}
