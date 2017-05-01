package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class CommandUseTable extends Table {

    private final String columnCommand;
    private final String columnTimesUsed;

    public CommandUseTable(SQLDB db, boolean usingMySQL) {
        super("plan_commandusages", db, usingMySQL);
        columnCommand = "command";
        columnTimesUsed = "times_used";
    }

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

    public HashMap<String, Integer> getCommandUse() throws SQLException {
        HashMap<String, Integer> commandUse = new HashMap<>();
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            while (set.next()) {
                commandUse.put(set.getString(columnCommand), set.getInt(columnTimesUsed));
            }
            return commandUse;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void saveCommandUse(HashMap<String, Integer> data) throws SQLException, NullPointerException {
        if (data.isEmpty()) {
            return;
        }
        PreparedStatement statement = null;
        try {
            removeAllData();
            statement = prepareStatement("INSERT INTO " + tableName + " ("
                    + columnCommand + ", "
                    + columnTimesUsed
                    + ") VALUES (?, ?)");
            boolean commitRequired = false;
            for (String key : data.keySet()) {
                if (key.length() > 20) {
                    continue;
                }
                statement.setString(1, key);
                statement.setInt(2, data.get(key));
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
