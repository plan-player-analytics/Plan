package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;

import java.io.File;
import java.sql.*;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Rsl1122
 */
public class SQLiteDB extends SQLDB {

    private final File databaseFile;
    private final String dbName;
    private Connection connection;
    private ITask connectionPingTask;

    public SQLiteDB(Supplier<Locale> locale) {
        this("database", locale);
    }

    public SQLiteDB(String dbName, Supplier<Locale> locale) {
        this(new File(PlanPlugin.getInstance().getDataFolder(), dbName + ".db"), locale);
    }

    public SQLiteDB(File databaseFile, Supplier<Locale> locale) {
        super(locale);
        dbName = databaseFile.getName();
        this.databaseFile = databaseFile;
    }

    @Override
    public void setupDataSource() throws DBInitException {
        try {
            connection = getNewConnection(databaseFile);
        } catch (SQLException e) {
            throw new DBInitException(e);
        }
        startConnectionPingTask();
    }

    public Connection getNewConnection(File dbFile) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            Log.toLog(this.getClass(), e);
            return null; // Should never happen.
        }

        String dbFilePath = dbFile.getAbsolutePath();

        Connection newConnection = getConnectionFor(dbFilePath);
        Log.debug("SQLite " + dbName + ": Opened a new Connection");
        newConnection.setAutoCommit(false);
        return newConnection;
    }

    private Connection getConnectionFor(String dbFilePath) throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath + "?journal_mode=WAL");
        } catch (SQLException ignored) {
            Log.info(locale.get().getString(PluginLang.DB_NOTIFY_SQLITE_WAL));
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        }
    }

    private void startConnectionPingTask() {
        stopConnectionPingTask();
        try {
            // Maintains Connection.
            connectionPingTask = RunnableFactory.createNew(new AbsRunnable("DBConnectionPingTask " + getName()) {
                @Override
                public void run() {
                    Statement statement = null;
                    ResultSet resultSet = null;
                    try {
                        if (connection != null && !connection.isClosed()) {
                            statement = connection.createStatement();
                            resultSet = statement.executeQuery("/* ping */ SELECT 1");
                        }
                    } catch (SQLException e) {
                        Log.debug("Something went wrong during SQLite Connection upkeep task.");
                        try {
                            connection = getNewConnection(databaseFile);
                        } catch (SQLException e1) {
                            Log.toLog(this.getClass(), e1);
                            Log.error("SQLite connection maintaining task had to be closed due to exception.");
                            this.cancel();
                        }
                    } finally {
                        MiscUtils.close(statement, resultSet);
                    }
                }
            }).runTaskTimerAsynchronously(60L * 20L, 60L * 20L);
        } catch (Exception ignored) {
        }
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
            connection = getNewConnection(databaseFile);
        }
        return connection;
    }

    @Override
    public void close() {
        stopConnectionPingTask();
        if (connection != null) {
            Log.debug("SQLite " + dbName + ": Closed Connection");
            MiscUtils.close(connection);
        }
        super.close();
    }

    @Override
    public void commit(Connection connection) {
        try {
            connection.commit();
        } catch (SQLException e) {
            if (!e.getMessage().contains("cannot commit")) {
                Log.toLog(this.getClass(), e);
            }
        }
    }

    @Override
    public void returnToPool(Connection connection) {
        // Connection pool not in use, no action required.
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SQLiteDB sqLiteDB = (SQLiteDB) o;
        return Objects.equals(dbName, sqLiteDB.dbName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dbName);
    }
}
