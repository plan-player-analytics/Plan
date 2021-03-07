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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import dagger.Lazy;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.Task;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author AuroraLS3
 */
public class SQLiteDB extends SQLDB {

    private final File databaseFile;
    private final String dbName;
    private Connection connection;
    private Task connectionPingTask;

    private SQLiteDB(
            File databaseFile,
            Locale locale,
            PlanConfig config,
            Lazy<ServerInfo> serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        super(() -> serverInfo.get().getServerUUID(), locale, config, runnableFactory, logger, errorLogger);
        dbName = databaseFile.getName();
        this.databaseFile = databaseFile;
    }

    @Override
    public void setupDataSource() {
        try {
            if (connection != null) connection.close();

            connection = getNewConnection(databaseFile);
        } catch (SQLException e) {
            throw new DBInitException(e.getMessage(), e);
        }
        startConnectionPingTask();
    }

    public Connection getNewConnection(File dbFile) throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            errorLogger.critical(e, ErrorContext.builder().whatToDo("Install SQLite Driver to the server").build());
            return null;
        }

        String dbFilePath = dbFile.getAbsolutePath();

        Connection newConnection = getConnectionFor(dbFilePath);
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
        return connection;
    }

    @Override
    public void close() {
        super.close();
        stopConnectionPingTask();

        if (connection != null) {
            MiscUtils.close(connection);
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
        private final RunnableFactory runnableFactory;
        private final PluginLogger logger;
        private final ErrorLogger errorLogger1;
        private final PlanFiles files;

        @Inject
        public Factory(
                Locale locale,
                PlanConfig config,
                PlanFiles files,
                Lazy<ServerInfo> serverInfo,
                RunnableFactory runnableFactory,
                PluginLogger logger,
                ErrorLogger errorLogger1
        ) {
            this.locale = locale;
            this.config = config;
            this.files = files;
            this.serverInfo = serverInfo;
            this.runnableFactory = runnableFactory;
            this.logger = logger;
            this.errorLogger1 = errorLogger1;
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
                    runnableFactory, logger, errorLogger1
            );
        }

    }

}
