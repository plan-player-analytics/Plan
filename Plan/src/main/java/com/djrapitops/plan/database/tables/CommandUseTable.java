package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;
import main.java.com.djrapitops.plan.utilities.Benchmark;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Rsl1122
 */
public class CommandUseTable extends Table {

    private final String columnCommandId = "id";
    private final String columnCommand = "command";
    private final String columnTimesUsed = "times_used";
    private final String columnServerID = "server_id";

    private final ServerTable serverTable;

    /**
     * @param db
     * @param usingMySQL
     */
    public CommandUseTable(SQLDB db, boolean usingMySQL) {
        super("plan_commandusages", db, usingMySQL);
        serverTable = db.getServerTable();
    }

    /**
     * @return
     */
    @Override
    public boolean createTable() {
        ServerTable serverTable = db.getServerTable();
        return createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnCommandId, Sql.INT)
                .column(columnCommand, Sql.varchar(20)).notNull()
                .column(columnTimesUsed, Sql.INT).notNull()
                .column(columnServerID, Sql.INT).notNull()
                .primaryKey(usingMySQL, columnCommandId)
                .foreignKey(columnServerID, serverTable.toString(), serverTable.getColumnID())
                .toString()
        );
    }

    /**
     * Used to get all commands used in this server.
     *
     * @return command - times used Map
     * @throws SQLException
     */
    public Map<String, Integer> getCommandUse() throws SQLException {
        return getCommandUse(Plan.getServerUUID());
    }

    /**
     * Used to get all commands used in a server.
     *
     * @param serverUUID UUID of the server.
     * @return command - times used Map
     * @throws SQLException
     */
    public Map<String, Integer> getCommandUse(UUID serverUUID) throws SQLException {
        Benchmark.start("Get CommandUse");
        Map<String, Integer> commandUse = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnCommand, columnTimesUsed)
                    .where(columnServerID + "=" + serverTable.statementSelectServerID)
                    .toString());
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            while (set.next()) {
                String cmd = set.getString(columnCommand).toLowerCase();
                int amountUsed = set.getInt(columnTimesUsed);
                Integer get = commandUse.get(cmd);
                if (get != null && get > amountUsed) {
                    continue;
                }
                commandUse.put(cmd, amountUsed);
            }
            return commandUse;
        } finally {
            endTransaction(statement);
            close(set, statement);
            Benchmark.stop("Database", "Get CommandUse");
        }
    }

    public void commandUsed(String command) throws SQLException {
        if (command.length() > 20) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("UPDATE " + tableName + " SET "
                    + columnTimesUsed + "=" + columnTimesUsed + "+ 1" +
                    " WHERE " + columnServerID + "=" + serverTable.statementSelectServerID +
                    " AND " + columnCommand + "=?");
            statement.setString(1, Plan.getServerUUID().toString());
            statement.setString(2, command);
            int success = statement.executeUpdate();

            commit(statement.getConnection());

            if (success == 0) {
                insertCommand(command);
            }
        } finally {
            close(statement);
        }
    }

    private void insertCommand(String command) throws SQLException {
        PreparedStatement statement = null;
        try {
            String insertStatement = "INSERT INTO " + tableName + " ("
                    + columnCommand + ", "
                    + columnTimesUsed + ", "
                    + columnServerID
                    + ") VALUES (?, ?, " + serverTable.statementSelectServerID + ")";
            statement = prepareStatement(insertStatement);
            statement.setString(1, command);
            statement.setInt(2, 1);
            statement.setString(3, Plan.getServerUUID().toString());
            statement.execute();

            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    public Optional<String> getCommandByID(int id) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnCommand).where(columnCommandId + "=?").toString());
            statement.setInt(1, id);
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(set.getString(columnCommand));
            }
            return Optional.empty();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public Optional<Integer> getCommandID(String command) throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName, columnCommandId).where(columnCommand + "=?").toString());
            statement.setString(1, command);
            set = statement.executeQuery();
            if (set.next()) {
                return Optional.of(set.getInt(columnCommandId));
            }
            return Optional.empty();
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }
}
