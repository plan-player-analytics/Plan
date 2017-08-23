package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
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
    private ServerTable serverTable;

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
        ServerTable serverTable = db.getServerTable();
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

    /**
     * @param data
     * @throws SQLException
     */
    public void saveCommandUse(Map<String, Integer> data) throws SQLException {
        if (Verify.isEmpty(data)) {
            return;
        }

        Benchmark.start("Save Commanduse");
        Map<String, Integer> newData = new HashMap<>(data);
        Map<String, Integer> saved = getCommandUse();
        newData.keySet().removeAll(saved.keySet());

        insertCommands(newData);

        Map<String, Integer> updateData = new HashMap<>(data);
        updateData.keySet().removeAll(newData.keySet());

        for (Map.Entry<String, Integer> savedEntry : saved.entrySet()) {
            String cmd = savedEntry.getKey();
            // IMPORTANT - not using saved as value
            Integer toSave = updateData.get(cmd);
            if (toSave != null && toSave <= savedEntry.getValue()) {
                updateData.remove(cmd);
            }
        }

        updateCommands(updateData);
        Benchmark.stop("Database", "Save Commanduse");
        db.setAvailable();
    }

    private void updateCommands(Map<String, Integer> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            String updateStatement = "UPDATE " + tableName + " SET " +
                    columnTimesUsed + "=? " +
                    "WHERE (" + columnCommand + "=?) AND (" +
                    columnServerID + "=" + serverTable.statementSelectServerID + ")";
            statement = prepareStatement(updateStatement);

            for (Map.Entry<String, Integer> entrySet : data.entrySet()) {
                String key = entrySet.getKey();
                Integer amount = entrySet.getValue();

                if (key.length() > 20) {
                    continue;
                }

                statement.setInt(1, amount);
                statement.setString(2, key);
                statement.setString(3, Plan.getServerUUID().toString());
                statement.addBatch();
            }

            statement.executeBatch();
            commit(statement.getConnection());
        } finally {
            close(statement);
        }
    }

    private void insertCommands(Map<String, Integer> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            String insertStatement = "INSERT INTO " + tableName + " ("
                    + columnCommand + ", "
                    + columnTimesUsed + ", "
                    + columnServerID
                    + ") VALUES (?, ?, " + serverTable.statementSelectServerID + ")";
            statement = prepareStatement(insertStatement);
            for (Map.Entry<String, Integer> entrySet : data.entrySet()) {
                String key = entrySet.getKey();
                Integer amount = entrySet.getValue();

                if (key.length() > 20) {
                    continue;
                }

                statement.setString(1, key);
                statement.setInt(2, amount);
                statement.setString(3, Plan.getServerUUID().toString());
                statement.addBatch();
            }

            statement.executeBatch();
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
