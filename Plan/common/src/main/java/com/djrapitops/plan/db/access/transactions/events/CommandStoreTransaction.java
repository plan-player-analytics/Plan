package com.djrapitops.plan.db.access.transactions.events;

import com.djrapitops.plan.db.access.ExecStatement;
import com.djrapitops.plan.db.access.transactions.Transaction;
import com.djrapitops.plan.db.sql.tables.CommandUseTable;
import com.djrapitops.plan.db.sql.tables.ServerTable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Transaction to update command usage information in the database.
 *
 * @author Rsl1122
 */
public class CommandStoreTransaction extends Transaction {

    private final UUID serverUUID;
    private final String commandName;

    public CommandStoreTransaction(
            UUID serverUUID,
            String commandName
    ) {
        this.serverUUID = serverUUID;
        this.commandName = commandName;
    }

    @Override
    protected boolean shouldBeExecuted() {
        return commandName.length() <= 20;
    }

    @Override
    protected void performOperations() {
        if (!updateCommandUse()) {
            insertCommand();
        }
    }

    private boolean updateCommandUse() {
        String sql = "UPDATE " + CommandUseTable.TABLE_NAME + " SET "
                + CommandUseTable.TIMES_USED + "=" + CommandUseTable.TIMES_USED + "+ 1" +
                " WHERE " + CommandUseTable.SERVER_ID + "=" + ServerTable.STATEMENT_SELECT_SERVER_ID +
                " AND " + CommandUseTable.COMMAND + "=?";

        return execute(new ExecStatement(sql) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, serverUUID.toString());
                statement.setString(2, commandName);
            }
        });
    }

    private void insertCommand() {
        execute(new ExecStatement(CommandUseTable.INSERT_STATEMENT) {
            @Override
            public void prepare(PreparedStatement statement) throws SQLException {
                statement.setString(1, commandName);
                statement.setInt(2, 1);
                statement.setString(3, serverUUID.toString());
            }
        });
    }
}