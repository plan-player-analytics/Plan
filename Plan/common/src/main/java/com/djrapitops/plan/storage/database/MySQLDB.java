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
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import dagger.Lazy;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author AuroraLS3
 */
@Singleton
public class MySQLDB extends SQLDB {

    private static int increment = 1;

    protected HikariDataSource dataSource;

    @Inject
    public MySQLDB(
            Locale locale,
            PlanConfig config,
            PlanFiles files,
            Lazy<ServerInfo> serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger pluginLogger,
            ErrorLogger errorLogger
    ) {
        super(() -> serverInfo.get().getServerUUID(), locale, config, files, runnableFactory, pluginLogger, errorLogger);
    }

    private static synchronized void increment() {
        increment++;
    }

    @Override
    public DBType getType() {
        return DBType.MYSQL;
    }

    @Override
    protected List<String> getDependencyResource() {
        try {
            return files.getResourceFromJar("dependencies/mysqlDriver.txt").asLines();
        } catch (IOException e) {
            throw new RuntimeException("Failed to get MySQL dependency information", e);
        }
    }

    /**
     * Setups the {@link HikariDataSource}
     */
    @Override
    public void setupDataSource() {
        if (driverClassLoader == null) {
            logger.info("Downloading MySQL Driver, this may take a while...");
            downloadDriver();
        }

        Thread currentThread = Thread.currentThread();
        ClassLoader previousClassLoader = currentThread.getContextClassLoader();

        // Set the context class loader to the driver class loader for Hikari to use for finding the Driver
        currentThread.setContextClassLoader(driverClassLoader);

        try {
            HikariConfig hikariConfig = new HikariConfig();

            String host = config.get(DatabaseSettings.MYSQL_HOST);
            String port = config.get(DatabaseSettings.MYSQL_PORT);
            String database = config.get(DatabaseSettings.MYSQL_DATABASE);
            String launchOptions = config.get(DatabaseSettings.MYSQL_LAUNCH_OPTIONS);
            // REGEX: match "?", match "word=word&" *-times, match "word=word"
            if (launchOptions.isEmpty() || !launchOptions.matches("\\?((([\\w-])+=.+)&)*(([\\w-])+=.+)")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
                logger.error(locale.getString(PluginLang.DB_MYSQL_LAUNCH_OPTIONS_FAIL, launchOptions));
            }
            hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + launchOptions);

            String username = config.get(DatabaseSettings.MYSQL_USER);
            String password = config.get(DatabaseSettings.MYSQL_PASS);

            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("connectionInitSql", "set time_zone = '+00:00'");

            hikariConfig.setPoolName("Plan Connection Pool-" + increment);
            increment();

            hikariConfig.setAutoCommit(false);
            try {
                hikariConfig.setMaximumPoolSize(config.get(DatabaseSettings.MAX_CONNECTIONS));
            } catch (IllegalStateException e) {
                logger.warn(e.getMessage() + ", using 1 as maximum for now.");
                hikariConfig.setMaximumPoolSize(1);
            }
            hikariConfig.setMaxLifetime(TimeUnit.MINUTES.toMillis(25L));
            hikariConfig.setLeakDetectionThreshold(TimeUnit.SECONDS.toMillis(29L));

            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            throw new DBInitException("Failed to set-up HikariCP Datasource: " + e.getMessage(), e);
        } finally {
            unloadMySQLDriver();
        }

        // Reset the context classloader back to what it was originally set to, now that the DataSource is created
        currentThread.setContextClassLoader(previousClassLoader);
    }

    private void unloadMySQLDriver() {
        // Avoid issues with other plugins by removing the mysql driver from driver manager
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            Class<?> driverClass = driver.getClass();
            // Checks that it's from our class loader to avoid unloading another plugin's/the server's driver
            if ("com.mysql.cj.jdbc.Driver".equals(driverClass.getName()) && driverClass.getClassLoader() == driverClassLoader) {
                try {
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public synchronized Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        if (!connection.isValid(5)) {
            connection.close();
            try {
                return getConnection();
            } catch (StackOverflowError databaseHasGoneDown) {
                throw new DBOpException("Valid connection could not be fetched (Is MySQL down?) - attempted until StackOverflowError occurred.", databaseHasGoneDown);
            }
        }
        if (connection.getAutoCommit()) connection.setAutoCommit(false);
        return connection;
    }

    @Override
    public void close() {
        super.close();

        if (dataSource != null) dataSource.close();
    }

    @Override
    public void returnToPool(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            errorLogger.critical(e, ErrorContext.builder().related("Closing connection").build());
        }
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
