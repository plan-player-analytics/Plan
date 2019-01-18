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
package com.djrapitops.plan.db;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.db.tasks.KeepAliveTask;
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
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
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
            NetworkContainer.Factory networkContainerFactory,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(() -> serverInfo.get().getServerUUID(), locale, config, networkContainerFactory, runnableFactory, logger, timings, errorHandler);
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
            connectionPingTask = runnableFactory.create("DBConnectionPingTask " + getType().getName(),
                    new KeepAliveTask(connection, () -> getNewConnection(databaseFile), logger, errorHandler)
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
        return connection;
    }

    @Override
    public void close() {
        super.close();
        stopConnectionPingTask();

        if (connection != null) {
            logger.debug("SQLite " + dbName + ": Closed Connection");
            MiscUtils.close(connection);
        }
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

    @Singleton
    public static class Factory {

        private final Locale locale;
        private final PlanConfig config;
        private final Lazy<ServerInfo> serverInfo;
        private final NetworkContainer.Factory networkContainerFactory;
        private final RunnableFactory runnableFactory;
        private final PluginLogger logger;
        private final Timings timings;
        private final ErrorHandler errorHandler;
        private PlanFiles files;

        @Inject
        public Factory(
                Locale locale,
                PlanConfig config,
                PlanFiles files,
                Lazy<ServerInfo> serverInfo,
                NetworkContainer.Factory networkContainerFactory,
                RunnableFactory runnableFactory,
                PluginLogger logger,
                Timings timings,
                ErrorHandler errorHandler
        ) {
            this.locale = locale;
            this.config = config;
            this.files = files;
            this.serverInfo = serverInfo;
            this.networkContainerFactory = networkContainerFactory;
            this.runnableFactory = runnableFactory;
            this.logger = logger;
            this.timings = timings;
            this.errorHandler = errorHandler;
        }

        public SQLiteDB usingDefaultFile() {
            return usingFileCalled("database");
        }

        public SQLiteDB usingFileCalled(String fileName) {
            return usingFile(files.getFileFromPluginFolder(fileName + ".db"));
        }

        public SQLiteDB usingFile(File databaseFile) {
            return new SQLiteDB(databaseFile,
                    locale, config, serverInfo,
                    networkContainerFactory,
                    runnableFactory, logger, timings, errorHandler
            );
        }

    }

}
