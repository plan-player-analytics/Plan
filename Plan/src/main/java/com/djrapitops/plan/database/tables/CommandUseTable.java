package main.java.com.djrapitops.plan.database.tables;

import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.exceptions.DBCreateTableException;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.database.sql.Select;
import main.java.com.djrapitops.plan.database.sql.Sql;
import main.java.com.djrapitops.plan.database.sql.TableSqlParser;

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
    private String insertStatement;

    /**
     * @param db
     * @param usingMySQL
     */
    public CommandUseTable(SQLDB db, boolean usingMySQL) {
        super("plan_commandusages", db, usingMySQL);
        serverTable = db.getServerTable();
        insertStatement = "INSERT INTO " + tableName + " ("
                + columnCommand + ", "
                + columnTimesUsed + ", "
                + columnServerID
                + ") VALUES (?, ?, " + serverTable.statementSelectServerID + ")";
    }

    /**
     * @return
     */
    @Override
    public void createTable() throws DBCreateTableException {
        ServerTable serverTable = db.getServerTable();
        createTable(TableSqlParser.createTable(tableName)
                .primaryKeyIDColumn(usingMySQL, columnCommandId)
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
        Map<String, Integer> commandUse = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement(Select.from(tableName,
                    columnCommand, columnTimesUsed)
                    .where(columnServerID + "=" + serverTable.statementSelectServerID)
                    .toString());
            statement.setFetchSize(5000);
            statement.setString(1, serverUUID.toString());
            set = statement.executeQuery();
            while (set.next()) {
                String cmd = set.getString(columnCommand).toLowerCase();
                int amountUsed = set.getInt(columnTimesUsed);
                commandUse.put(cmd, amountUsed);
            }
            return commandUse;
        } finally {
            endTransaction(statement);
            close(set, statement);
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

    public Map<UUID, Map<String, Integer>> getAllCommandUsages() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            String serverIDColumn = serverTable + "." + serverTable.getColumnID();
            String serverUUIDColumn = serverTable + "." + serverTable.getColumnUUID() + " as s_uuid";
            statement = prepareStatement("SELECT " +
                    columnCommand + ", " +
                    columnTimesUsed + ", " +
                    serverUUIDColumn +
                    " FROM " + tableName +
                    " JOIN " + serverTable + " on " + serverIDColumn + "=" + columnServerID
            );
            statement.setFetchSize(5000);
            set = statement.executeQuery();
            Map<UUID, Map<String, Integer>> map = new HashMap<>();
            while (set.next()) {
                UUID serverUUID = UUID.fromString(set.getString("s_uuid"));

                Map<String, Integer> serverMap = map.getOrDefault(serverUUID, new HashMap<>());

                String command = set.getString(columnCommand);
                int timesUsed = set.getInt(columnTimesUsed);

                serverMap.put(command, timesUsed);
                map.put(serverUUID, serverMap);
            }
            return map;
        } finally {
            endTransaction(statement);
            close(set, statement);
        }
    }

    public void insertCommandUsage(Map<UUID, Map<String, Integer>> allCommandUsages) throws SQLException {
        if (allCommandUsages.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            statement = prepareStatement(insertStatement);

            // Every Server
            for (UUID serverUUID : allCommandUsages.keySet()) {
                // Every Command
                for (Map.Entry<String, Integer> entry : allCommandUsages.get(serverUUID).entrySet()) {
                    String command = entry.getKey();
                    int timesUsed = entry.getValue();

                    statement.setString(1, command);
                    statement.setInt(2, timesUsed);
                    statement.setString(3, serverUUID.toString());
                    statement.addBatch();
                }
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }
}
