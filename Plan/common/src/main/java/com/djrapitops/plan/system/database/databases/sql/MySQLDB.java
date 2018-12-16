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
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.system.database.databases.DBType;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dagger.Lazy;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

    private static int increment = 1;

    protected DataSource dataSource;

    @Inject
    public MySQLDB(
            Locale locale,
            PlanConfig config,
            Lazy<ServerInfo> serverInfo,
            NetworkContainer.Factory networkContainerFactory,
            RunnableFactory runnableFactory,
            PluginLogger pluginLogger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        super(() -> serverInfo.get().getServerUUID(), locale, config, networkContainerFactory, runnableFactory, pluginLogger, timings, errorHandler);
    }

    private static synchronized void increment() {
        increment++;
    }

    @Override
    public DBType getType() {
        return DBType.MYSQL;
    }

    /**
     * Setups the {@link HikariDataSource}
     */
    @Override
    public void setupDataSource() throws DBInitException {
        try {
            HikariConfig hikariConfig = new HikariConfig();

            String host = config.get(DatabaseSettings.MYSQL_HOST);
            String port = config.get(DatabaseSettings.MYSQL_PORT);
            String database = config.get(DatabaseSettings.MYSQL_DATABASE);
            String launchOptions = config.get(DatabaseSettings.MYSQL_LAUNCH_OPTIONS);
            if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
                logger.error(locale.getString(PluginLang.DB_MYSQL_LAUNCH_OPTIONS_FAIL, launchOptions));
            }
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + launchOptions);

            String username = config.get(DatabaseSettings.MYSQL_USER);
            String password = config.get(DatabaseSettings.MYSQL_PASS);

            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);

            hikariConfig.setPoolName("Plan Connection Pool-" + increment);
            increment();

            hikariConfig.setAutoCommit(true);
            hikariConfig.setMaximumPoolSize(8);
            hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(25L));
            hikariConfig.setLeakDetectionThreshold(TimeUnit.MINUTES.toMillis(10L));

            this.dataSource = new HikariDataSource(hikariConfig);

            getConnection();
        } catch (SQLException e) {
            throw new DBInitException("Failed to set-up HikariCP Datasource: " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (!connection.isValid(5)) {
            connection.close();
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
            try {
                setupDataSource();
                // get new connection after restarting pool
                return dataSource.getConnection();
            } catch (DBInitException e) {
                throw new DBOpException("Failed to restart DataSource after a connection was invalid: " + e.getMessage(), e);
            }
        }
        return connection;
    }

    @Override
    public void close() {
        super.close();

        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }

    @Override
    public void returnToPool(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    @Override
    public void commit(Connection connection) {
        returnToPool(connection);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MySQLDB mySQLDB = (MySQLDB) o;
        return Objects.equals(dataSource, mySQLDB.dataSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dataSource);
    }
}
