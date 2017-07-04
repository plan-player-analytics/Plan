package main.java.com.djrapitops.plan.database.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;

/**
 *
 * @author Rsl1122
 */
public abstract class Table {

    /**
     *
     */
    protected String tableName;

    /**
     *
     */
    protected SQLDB db;

    /**
     *
     */
    protected boolean usingMySQL;

    /**
     *
     * @param name
     * @param db
     * @param usingMySQL
     */
    public Table(String name, SQLDB db, boolean usingMySQL) {
        this.tableName = name;
        this.db = db;
        this.usingMySQL = usingMySQL;
    }

    /**
     *
     * @return
     */
    public abstract boolean createTable();

    /**
     *
     * @return @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        Connection connection = db.getConnection();
        if (connection == null || connection.isClosed()) {
            connection = db.getNewConnection();
        }
        return connection;
    }

    /**
     *
     * @return @throws SQLException
     */
    public int getVersion() throws SQLException {
        return db.getVersion();
    }

    /**
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    protected boolean execute(String sql) throws SQLException {
        Connection connection = getConnection();
        boolean success = connection.createStatement().execute(sql);
        return success;
    }

    /**
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    protected PreparedStatement prepareStatement(String sql) throws SQLException {
        return getConnection().prepareStatement(sql);
    }

    /**
     *
     * @param toClose
     */
    protected void close(AutoCloseable toClose) {
        if (toClose != null) {
            try {
                toClose.close();
            } catch (Exception ex) {
            }
        }
    }

    /**
     *
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
     *
     * @return
     */
    public boolean removeAllData() {
        try {
            execute("DELETE FROM " + tableName);
            return true;
        } catch (SQLException ex) {
            Log.toLog(this.getClass().getName(), ex);
            return false;
        }
    }

    /**
     *
     * @param <T>
     * @param objects
     * @return
     */
    protected <T> List<List<Container<T>>> splitIntoBatches(Map<Integer, List<T>> objects) {
        return DBUtils.splitIntoBatchesId(objects);
    }
}
