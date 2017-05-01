package main.java.com.djrapitops.plan.database.tables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public class VersionTable extends Table {

    public VersionTable(SQLDB db, boolean usingMySQL) {
        super("plan_version", db, usingMySQL);
    }

    @Override
    public boolean createTable() {
        try {
            execute("CREATE TABLE IF NOT EXISTS " + tableName + " ("
                    + "version integer NOT NULL"
                    + ")"
            );
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    @Override
    public int getVersion() throws SQLException {
        PreparedStatement statement = null;
        ResultSet set = null;
        try {
            int version = 0;
            statement = prepareStatement("SELECT * FROM " + tableName);
            set = statement.executeQuery();
            if (set.next()) {
                version = set.getInt("version");
            }
            return version;
        } finally {
            close(set);
            close(statement);
        }
    }

    public void setVersion(int version) throws SQLException {
        removeAllData();
        PreparedStatement statement = null;
        try {
            statement = prepareStatement("INSERT INTO " + tableName + " (version) VALUES (" + version + ")");
            statement.executeUpdate();
        } finally {
            close(statement);
        }
    }

}
