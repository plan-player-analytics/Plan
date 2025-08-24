/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.upkeep.DBKeepAliveTask;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.SemaphoreAccessCounter;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Lazy;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.Task;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author AuroraLS3
 */
public class SQLiteDB extends SQLDB {

    private final File databaseFile;
    private final String dbName;
    private Connection connection;
    private Task connectionPingTask;

    /*
     * In charge of keeping a single thread in control of the connection to avoid
     * one thread closing the connection while another is executing a statement as
     * that might lead to a SIGSEGV signal JVM crash.
     */
    private final SemaphoreAccessCounter connectionLock;

    private Constructor<?> connectionConstructor;

    private SQLiteDB(
            File databaseFile,
            Locale locale,
            PlanConfig config,
            PlanFiles files,
            Lazy<ServerInfo> serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorLogger errorLogger,
            ApplicationDependencyManager applicationDependencyManager
    ) {
        super(
                () -> serverInfo.get().getServerUUID(),
                locale,
                config,
                files,
                runnableFactory,
                logger,
                errorLogger,
                applicationDependencyManager
        );
        dbName = databaseFile.getName();
        this.databaseFile = databaseFile;
        connectionLock = new SemaphoreAccessCounter();
    }

    @Override
    protected List<String> getDependencyResource() {
        try {
            return files.getResourceFromJar("dependencies/sqliteDriver.txt").asLines();
        } catch (IOException e) {
            throw new DBInitException("Failed to get SQLite dependency information: " + e.getMessage(), e);
        }
    }

    @Override
    public void setupDataSource() {
        try {
            if (connection != null) connection.close();

            connection = getNewConnection(databaseFile);
        } catch (SQLException e) {
            throw new DBInitException(e.toString(), e);
        }
        startConnectionPingTask();
    }

    public Connection getNewConnection(File dbFile) throws SQLException {
        if (driverClassLoader == null) {
            logger.info("Downloading SQLite Driver, this may take a while...");
            downloadDriver();
        }
        String dbFilePath = dbFile.getAbsolutePath();

        Connection newConnection = getConnectionFor(dbFilePath);
        newConnection.setAutoCommit(false);
        return newConnection;
    }

    private Connection getConnectionFor(String dbFilePath) throws SQLException {
        ensureConstructorIsAvailable();
        return tryToConnect(dbFilePath, true);
    }

    private void ensureConstructorIsAvailable() {
        if (connectionConstructor != null) {
            return;
        }

        try {
            Class<?> connectionClass = driverClassLoader.loadClass("org.sqlite.jdbc4.JDBC4Connection");
            connectionConstructor = connectionClass.getConstructor(String.class, String.class, Properties.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new DBInitException("Failed to initialize SQLite Driver", e);
        }
    }

    private Connection tryToConnect(String dbFilePath, boolean withWAL) throws SQLException {
        try {
            Properties properties = new Properties();
            if (withWAL) properties.put("journal_mode", "WAL");

            return (Connection) connectionConstructor.newInstance("jdbc:sqlite:" + dbFilePath, dbFilePath, properties);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (!withWAL && cause instanceof SQLException) {
                throw (SQLException) cause;
            } else if (!(cause instanceof SQLException)) {
                throw new DBInitException("Failed to initialize SQLite Driver", cause);
            }

            // Run the method again with withWAL set to false, if it fails again, an exception will be thrown above
            logger.info(locale.getString(PluginLang.DB_NOTIFY_SQLITE_WAL));
            return tryToConnect(dbFilePath, false);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DBInitException("Failed to initialize SQLite Driver", e);
        }
    }

    private void startConnectionPingTask() {
        stopConnectionPingTask();
        try {
            // Maintains Connection.
            connectionPingTask = runnableFactory.create(
                    new DBKeepAliveTask(connection, () -> getNewConnection(databaseFile), logger, errorLogger)
            ).runTaskTimerAsynchronously(60L * 20L, 60L * 20L);
        } catch (Exception ignored) {
            // Task failed to register because plugin is being disabled
        }
    }

    private void stopConnectionPingTask() {
        if (connectionPingTask != null) {
            try {
                connectionPingTask.cancel();
            } catch (Exception ignored) {
                // Sometimes task systems fail to cancel a task,
                // usually this is called on disable, so no need for users to report this.
            }
        }
    }

    @Override
    public DBType getType() {
        return DBType.SQLITE;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection == null) {
            connection = getNewConnection(databaseFile);
        }
        connectionLock.enter();
        return connection;
    }

    @Override
    public void close() {
        if (getState() == State.OPEN) setState(State.CLOSING);
        boolean transactionQueueClosed = attemptToCloseTransactionExecutor();
        if (transactionQueueClosed) logger.info(locale.getString(PluginLang.DISABLED_WAITING_TRANSACTIONS_COMPLETE));

        unloadDriverClassloader();
        setState(State.CLOSED);

        stopConnectionPingTask();

        logger.info(locale.getString(PluginLang.DISABLED_WAITING_SQLITE));
        connectionLock.waitUntilNothingAccessing();

        // Transaction queue can't be force-closed before all connections have terminated.
        if (!transactionQueueClosed) forceCloseTransactionExecutor();

        if (connection != null) {
            MiscUtils.close(connection);
        }
        logger.info(locale.getString(PluginLang.DISABLED_WAITING_SQLITE_COMPLETE));
    }

    @Override
    public void returnToPool(Connection connection) {
        connectionLock.exit();
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

    @Singleton
    public static class Factory {

        private final Locale locale;
        private final PlanConfig config;
        private final Lazy<ServerInfo> serverInfo;
        private final RunnableFactory runnableFactory;
        private final PluginLogger logger;
        private final ErrorLogger errorLogger1;
        private final PlanFiles files;
        private final ApplicationDependencyManager applicationDependencyManager;

        @Inject
        public Factory(
                Locale locale,
                PlanConfig config,
                PlanFiles files,
                Lazy<ServerInfo> serverInfo,
                RunnableFactory runnableFactory,
                PluginLogger logger,
                ErrorLogger errorLogger1,
                ApplicationDependencyManager applicationDependencyManager
        ) {
            this.locale = locale;
            this.config = config;
            this.files = files;
            this.serverInfo = serverInfo;
            this.runnableFactory = runnableFactory;
            this.logger = logger;
            this.errorLogger1 = errorLogger1;
            this.applicationDependencyManager = applicationDependencyManager;
        }

        public SQLiteDB usingDefaultFile() {
            return usingFileCalled("database");
        }

        public SQLiteDB usingFileCalled(String fileName) {
            return usingFile(files.getFileFromPluginFolder(fileName + ".db"));
        }

        public SQLiteDB usingFile(File databaseFile) {
            return new SQLiteDB(databaseFile,
                    locale, config, files, serverInfo,
                    runnableFactory, logger, errorLogger1,
                    applicationDependencyManager
            );
        }

    }

}
