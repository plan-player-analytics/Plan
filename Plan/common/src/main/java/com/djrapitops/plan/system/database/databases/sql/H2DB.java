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
package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.system.database.databases.DBType;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Lazy;
import org.h2.jdbcx.JdbcDataSource;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Implementation of the H2 database
 *
 * @author Fuzzlemann
 */
public class H2DB extends SQLDB {

    private final File databaseFile;
    private final String dbName;
    private Connection connection;
    private PluginTask connectionPingTask;

    private H2DB(
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

        execute("SET REFERENTIAL_INTEGRITY FALSE");
        startConnectionPingTask();
    }

    public Connection getNewConnection(File dbFile) throws SQLException {
        String dbFilePath = dbFile.getAbsolutePath();

        Connection newConnection = getConnectionFor(dbFilePath);
        logger.debug("H2 " + dbName + ": Opened a new Connection");
        newConnection.setAutoCommit(false);
        return newConnection;
    }

    private Connection getConnectionFor(String dbFilePath) throws SQLException {
        String username = config.get(DatabaseSettings.MYSQL_USER);
        String password = config.get(DatabaseSettings.MYSQL_PASS);

        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:file:" + dbFilePath + ";mode=MySQL");
        jdbcDataSource.setUser(username);
        jdbcDataSource.setPassword(password);

        return jdbcDataSource.getConnection();
    }

    private void startConnectionPingTask() {
        stopConnectionPingTask();
        // Maintains Connection.
        connectionPingTask = runnableFactory.create("DBConnectionPingTask " + getType().getName(),
                new KeepAliveTask(connection, () -> getNewConnection(databaseFile), logger, errorHandler)
        ).runTaskTimerAsynchronously(60L * 20L, 60L * 20L);
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
        return DBType.H2;
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
            logger.debug("H2DB " + dbName + ": Closed Connection");
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
        H2DB h2DB = (H2DB) o;
        return Objects.equals(dbName, h2DB.dbName);
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

        public H2DB usingDefaultFile() {
            return usingFileCalled("h2database");
        }

        public H2DB usingFileCalled(String fileName) {
            return usingFile(files.getFileFromPluginFolder(fileName));
        }

        public H2DB usingFile(File databaseFile) {
            return new H2DB(databaseFile,
                    locale, config, serverInfo,
                    networkContainerFactory,
                    runnableFactory, logger, timings, errorHandler
            );
        }

    }
}
