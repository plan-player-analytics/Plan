package com.djrapitops.plan.database.databases;

import com.djrapitops.plan.api.exceptions.DatabaseInitException;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Rsl1122
 */
public class SQLiteDB extends SQLDB {

    private final String dbName;
    private Connection connection;
    private ITask connectionPingTask;

    /**
     * Class Constructor.
     */
    public SQLiteDB() {
        this("database");
    }

    public SQLiteDB(String dbName) {
        this.dbName = dbName;
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() throws DatabaseInitException {
        try {
            connection = getNewConnection(dbName);
        } catch (SQLException e) {
            throw new DatabaseInitException(e);
        }
        startConnectionPingTask();
    }

    public Connection getNewConnection(String dbName) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Log.toLog(this.getClass().getName(), e);
            return null; // Should never happen.
        }

        String dbFilePath = new File(MiscUtils.getIPlan().getDataFolder(), dbName + ".db").getAbsolutePath();
        Connection connection;

        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath + "?journal_mode=WAL");
        } catch (SQLException ignored) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            Log.info("SQLite WAL mode not supported on this server version, using default. This may or may not affect performance.");
        }
        connection.setAutoCommit(false);
//        connection.

//        setJournalMode(connection);

        return connection;
    }

    private void setJournalMode(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("");
        }
    }

    private void startConnectionPingTask() {
        stopConnectionPingTask();

        // Maintains Connection.
        connectionPingTask = RunnableFactory.createNew(new AbsRunnable("DBConnectionPingTask " + getName()) {
            @Override
            public void run() {
                Statement statement = null;
                try {
                    if (connection != null && !connection.isClosed()) {
                        statement = connection.createStatement();
                        statement.execute("/* ping */ SELECT 1");
                    }
                } catch (SQLException e) {
                    try {
                        connection = getNewConnection(dbName);
                    } catch (SQLException e1) {
                        Log.toLog(this.getClass().getName(), e1);
                        Log.error("SQLite connection maintaining task had to be closed due to exception.");
                        this.cancel();
                    }
                } finally {
                    MiscUtils.close(statement);
                }
            }
        }).runTaskTimerAsynchronously(60L * 20L, 60L * 20L);
    }

    private void stopConnectionPingTask() {
        if (connectionPingTask != null) {
            try {
                connectionPingTask.cancel();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * @return the name of the Database
     */
    @Override
    public String getName() {
        return "SQLite";
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = getNewConnection(dbName);
        }
        return connection;
    }

    @Override
    public void close() throws SQLException {
        stopConnectionPingTask();
        MiscUtils.close(connection);
        super.close();
    }
}
