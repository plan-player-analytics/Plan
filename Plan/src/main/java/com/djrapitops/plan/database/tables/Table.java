package main.java.com.djrapitops.plan.database.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public abstract class Table {

    String tableName;
    SQLDB db;
    boolean usingMySQL;

    public Table(String name, SQLDB db, boolean usingMySQL) {
        this.tableName = name;
        this.db = db;
        this.usingMySQL = usingMySQL;
    }
    
    public abstract boolean createTable();
    
    public Connection getConnection() throws SQLException {
        Connection connection = db.getConnection();
        if (connection == null || connection.isClosed()) {
            connection = db.getNewConnection();
        }
        return connection;
    }
    
    public int getVersion() throws SQLException {
        return db.getVersion();
    }

    public boolean execute(String sql) throws SQLException {
        Connection connection = getConnection();
        boolean success = connection.createStatement().execute(sql);
        return success;
    }
    
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }
    
    public void close(AutoCloseable toClose) {
        if (toClose != null) {
            try {
                toClose.close();
            } catch (Exception ex) {
            }
        }
    }

    public String getTableName() {
        return tableName;
    }
    
    public boolean removeAllData() {
        try {
            execute("DELETE FROM " + tableName);
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }
}
