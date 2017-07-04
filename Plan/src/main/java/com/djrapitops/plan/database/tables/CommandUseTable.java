package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.Benchmark;

/**
 *
 * @author Rsl1122
 */
public class CommandUseTable extends Table {

    private final String columnCommand;
    private final String columnTimesUsed;

    /**
     *
     * @param db
     * @param usingMySQL
     */
    public CommandUseTable(SQLDB db, boolean usingMySQL) {
        super("plan_commandusages", db, usingMySQL);
        columnCommand = "command";
        columnTimesUsed = "times_used";
    }

    /**
     *
     * @return
     */
    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + columnCommand + " varchar(20) NOT NULL, "
                    + columnTimesUsed + " integer NOT NULL"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     *
     * @return @throws SQLException
     */
    public Map<String, Integer> getCommandUse() throws SQLException {
        Benchmark.start("Get CommandUse");
        Map<String, Integer> commandUse = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
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
            close(set);
            close(statement);
            Benchmark.stop("Get CommandUse");
        }
    }

    /**
     *
     * @param data
     * @throws SQLException
     * @throws NullPointerException
     */
    public void saveCommandUse(Map<String, Integer> data) throws SQLException, NullPointerException {
        if (data.isEmpty()) {
            return;
        }
        Benchmark.start("Save Commanduse");
        Map<String, Integer> newData = new HashMap<>(data);
        Map<String, Integer> saved = getCommandUse();
        newData.keySet().removeAll(saved.keySet());
        insertCommands(newData);
        Map<String, Integer> updateData = new HashMap<>(data);
        updateData.keySet().removeAll(newData.keySet());
        for (String cmd : saved.keySet()) {
            Integer toSave = updateData.get(cmd);
            if (toSave != null) {
                if (toSave <= saved.get(cmd)) {
                    updateData.remove(cmd);
                }
            }
        }
        updateCommands(updateData);
        Benchmark.stop("Save Commanduse");
    }

    private void updateCommands(Map<String, Integer> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            String updateStatement = "UPDATE " + tableName + " SET " + columnTimesUsed + "=? WHERE (" + columnCommand + "=?)";
            statement = prepareStatement(updateStatement);
            boolean commitRequired = false;
            for (String key : data.keySet()) {
                Integer amount = data.get(key);
                if (key.length() > 20) {
                    continue;
                }
                statement.setInt(1, amount);
                statement.setString(2, key);
                statement.addBatch();
                commitRequired = true;
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }

    private void insertCommands(Map<String, Integer> data) throws SQLException {
        PreparedStatement statement = null;
        try {
            String insertStatement = "INSERT INTO " + tableName + " ("
                    + columnCommand + ", "
                    + columnTimesUsed
                    + ") VALUES (?, ?)";
            statement = prepareStatement(insertStatement);
            boolean commitRequired = false;
            for (String key : data.keySet()) {
                Integer amount = data.get(key);
                if (key.length() > 20) {
                    continue;
                }
                statement.setString(1, key);
                statement.setInt(2, amount);
                statement.addBatch();
                commitRequired = true;
            }
            if (commitRequired) {
                statement.executeBatch();
            }
        } finally {
            close(statement);
        }
    }
}
