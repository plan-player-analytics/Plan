package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import java.io.File;
import java.sql.*;
import java.util.Objects;

/**
 * @author Rsl1122
 */
public class SQLiteDB extends SQLDB {

    private final File databaseFile;
    private final String dbName;
    private Connection connection;
    private PluginTask connectionPingTask;

    private SQLiteDB(
            File databaseFile,
            Locale locale,
            PlanConfig config,
            Lazy<ServerInfo> serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(() -> serverInfo.get().getServerUUID(), locale, config, runnableFactory, logger, timings, errorHandler);
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
            errorHandler.log(L.CRITICAL, this.getClass(), e);
            return null; // Should never happen.
        }

        String dbFilePath = dbFile.getAbsolutePath();

        Connection newConnection = getConnectionFor(dbFilePath);
        logger.debug("SQLite " + dbName + ": Opened a new Connection");
        newConnection.setAutoCommit(false);
        return newConnection;
    }

    private Connection getConnectionFor(String dbFilePath) throws SQLException {
        try {
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath + "?journal_mode=WAL");
        } catch (SQLException ignored) {
            logger.info(locale.getString(PluginLang.DB_NOTIFY_SQLITE_WAL));
            return DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        }
    }

    private void startConnectionPingTask() {
        stopConnectionPingTask();
        try {
            // Maintains Connection.
            connectionPingTask = runnableFactory.create("DBConnectionPingTask " + getName(), new AbsRunnable() {
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
                        logger.debug("Something went wrong during SQLite Connection upkeep task.");
                        try {
                            connection = getNewConnection(databaseFile);
                        } catch (SQLException e1) {
                            errorHandler.log(L.ERROR, this.getClass(), e1);
                            logger.error("SQLite connection maintaining task had to be closed due to exception.");
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
            logger.debug("SQLite " + dbName + ": Closed Connection");
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
                errorHandler.log(L.ERROR, this.getClass(), e);
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

    public static class Factory {

        private final Locale locale;
        private final PlanConfig config;
        private final Lazy<ServerInfo> serverInfo;
        private final RunnableFactory runnableFactory;
        private final PluginLogger logger;
        private final Timings timings;
        private final ErrorHandler errorHandler;
        private PlanFiles planFiles;

        @Inject
        public Factory(
                Locale locale,
                PlanConfig config,
                PlanFiles planFiles,
                Lazy<ServerInfo> serverInfo,
                RunnableFactory runnableFactory,
                PluginLogger logger,
                Timings timings,
                ErrorHandler errorHandler
        ) {
            this.locale = locale;
            this.config = config;
            this.planFiles = planFiles;
            this.serverInfo = serverInfo;
            this.runnableFactory = runnableFactory;
            this.logger = logger;
            this.timings = timings;
            this.errorHandler = errorHandler;
        }

        public SQLiteDB usingDefaultFile() {
            return usingFileCalled("database");
        }

        public SQLiteDB usingFileCalled(String fileName) {
            return usingFile(planFiles.getFileFromPluginFolder(fileName + ".db"));
        }

        public SQLiteDB usingFile(File databaseFile) {
            return new SQLiteDB(databaseFile, locale, config, serverInfo, runnableFactory, logger, timings, errorHandler);
        }

    }
}
