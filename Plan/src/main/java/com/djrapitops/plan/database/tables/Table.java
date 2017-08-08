package main.java.com.djrapitops.plan.database.tables;

import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.database.Container;
import main.java.com.djrapitops.plan.database.DBUtils;
import main.java.com.djrapitops.plan.database.databases.SQLDB;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * @author Rsl1122
 */
public abstract class Table {

    /**
     *
     */
    protected final String tableName;

    /**
     *
     */
    protected final SQLDB db;

    /**
     *
     */
    protected final boolean usingMySQL;

    /**
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
     * @return
     */
    public abstract boolean createTable();

    /**
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
     * @return @throws SQLException
     */
    public int getVersion() throws SQLException {
        return db.getVersion();
    }

    /**
     * @param statementString
     * @return
     * @throws SQLException
     */
    protected boolean execute(String statementString) throws SQLException {
        Connection connection = getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();
            return statement.execute(statementString);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * Used to execute queries while possible SQLExceptions are suppressed.
     *
     * @param statements SQL statements to execute
     */
    protected void executeUnsafe(String... statements) {
        Verify.nullCheck(statements);
        for (String statement : statements) {
            try {
                execute(statement);
            } catch (SQLException ignored) {
            }
        }
    }

    /**
     * @param sql
     * @return
     * @throws SQLException
     */
    protected PreparedStatement prepareStatement(String sql) throws SQLException {
        Log.debug(sql);
        return getConnection().prepareStatement(sql);
    }

    /**
     * @param toClose
     */
    protected void close(AutoCloseable... toClose) {
        MiscUtils.close(toClose);
    }

    /**
     * @return
     */
    public String getTableName() {
        return tableName;
    }

    /**
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
     * @param <T>
     * @param objects
     * @return
     */
    protected <T> List<List<Container<T>>> splitIntoBatches(Map<Integer, List<T>> objects) {
        return DBUtils.splitIntoBatchesId(objects);
    }

    protected void addColumns(String... columnInfo) {
        for (int i = 0; i < columnInfo.length; i++) {
            columnInfo[i] = "ALTER TABLE " + tableName + " ADD " + (usingMySQL ? "" : "COLUMN ") + columnInfo[i];
        }
        executeUnsafe(columnInfo);
    }

    protected void removeColumns(String... columnNames) {
        if (usingMySQL) {
            StringBuilder sqlBuild = new StringBuilder();
            sqlBuild.append("ALTER TABLE ").append(tableName);
            for (int i = 0; i < columnNames.length; i++) {
                sqlBuild.append(" DROP COLUMN ").append(columnNames[i]);
                if (i < columnNames.length - 1) {
                    sqlBuild.append(",");
                }
            }
            executeUnsafe(sqlBuild.toString());
        }
    }

    @Override
    public String toString() {
        return tableName;
    }

    /**
     * Commits changes to .db file when using SQLite databse.
     *
     * Auto Commit enabled when using MySQL
     * @throws SQLException If commit fails or there is nothing to commit.
     */
    protected void commit() throws SQLException {
        db.commit();
    }
}
