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
import com.djrapitops.plan.db.access.QueryStatement;
import com.djrapitops.plan.db.sql.parsing.CreateTableParser;
import com.djrapitops.plan.db.sql.parsing.Select;
import com.djrapitops.plan.db.sql.parsing.Sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
        serverTable = db.getServerTable();

    }

    private final ServerTable serverTable;

    public static String createTableSQL(DBType dbType) {
        return CreateTableParser.create(TABLE_NAME, dbType)
                .column(COMMAND_ID, Sql.INT).primaryKey()
                .column(COMMAND, Sql.varchar(20)).notNull()
                .column(TIMES_USED, Sql.INT).notNull()
                .column(SERVER_ID, Sql.INT).notNull()
                .foreignKey(SERVER_ID, ServerTable.TABLE_NAME, ServerTable.SERVER_ID)
                .toString();
    }

    /**
     * Used to get all commands used in a server.
     *
     * @param serverUUID UUID of the server.
     * @return command - times used Map
     */
    public Map<String, Integer> getCommandUse(UUID serverUUID) {
        String sql = Select.from(tableName,
                COMMAND, TIMES_USED)
                .where(SERVER_ID + "=" + serverTable.statementSelectServerID)
                .toString();

        return query(new QueryStatement<Map<String, Integer>>(sql, 5000) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
            }

            @Override
            public Map<String, Integer> processResults(ResultSet set) throws SQLException {
                Map<String, Integer> commandUse = new HashMap<>();
                while (set.next()) {
                    String cmd = set.getString(COMMAND).toLowerCase();
                    int amountUsed = set.getInt(TIMES_USED);
                    commandUse.put(cmd, amountUsed);
                }
                return commandUse;
            }
        });
    }

    public void commandUsed(String command) {
        if (command.length() > 20) {
            return;
        }

        String sql = "UPDATE " + tableName + " SET "
                + TIMES_USED + "=" + TIMES_USED + "+ 1" +
                " WHERE " + SERVER_ID + "=" + serverTable.statementSelectServerID +
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

    public Optional<String> getCommandByID(int id) {
        String sql = Select.from(tableName, COMMAND).where(COMMAND_ID + "=?").toString();

        return query(new QueryStatement<Optional<String>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setInt(1, id);
            }

            @Override
            public Optional<String> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getString(COMMAND));
                }
                return Optional.empty();
            }
        });
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

    public Optional<Integer> getCommandID(String command) {
        String sql = Select.from(tableName, COMMAND_ID).where(COMMAND + "=?").toString();

        return query(new QueryStatement<Optional<Integer>>(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, command);
            }

            @Override
            public Optional<Integer> processResults(ResultSet set) throws SQLException {
                if (set.next()) {
                    return Optional.of(set.getInt(COMMAND_ID));
                }
                return Optional.empty();
            }
        });
    }
}
