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
import com.djrapitops.plan.exceptions.database.MariaDB11Exception;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.queries.schema.MySQLSchemaQueries;
import com.djrapitops.plan.storage.database.transactions.init.OperationCriticalTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import dagger.Lazy;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.sql.*;
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

    private static boolean useMariaDbDriver = false;

    protected HikariDataSource dataSource;

    @Inject
    public MySQLDB(
            Locale locale,
            PlanConfig config,
            PlanFiles files,
            Lazy<ServerInfo> serverInfo,
            RunnableFactory runnableFactory,
            PluginLogger pluginLogger,
            ErrorLogger errorLogger,
            ApplicationDependencyManager applicationDependencyManager
    ) {
        super(
                () -> serverInfo.get().getServerUUID(),
                locale,
                config,
                files,
                runnableFactory,
                pluginLogger,
                errorLogger,
                applicationDependencyManager
        );
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
            String driverFile = useMariaDbDriver ? "dependencies/mariadbDriver.txt" : "dependencies/mysqlDriver.txt";
            return files.getResourceFromJar(driverFile).asLines();
        } catch (IOException e) {
            throw new DBInitException("Failed to get " + (useMariaDbDriver ? "MariaDB" : "MySQL") + " dependency information", e);
        }
    }

    /**
     * Setups the {@link HikariDataSource}
     */
    @Override
    public void setupDataSource() {
        if (driverClassLoader == null) {
            logger.info("Downloading " + (useMariaDbDriver ? "MariaDB" : "MySQL") + " Driver, this may take a while...");
            downloadDriver();
        }

        Thread currentThread = Thread.currentThread();
        ClassLoader previousClassLoader = currentThread.getContextClassLoader();

        // Set the context class loader to the driver class loader for Hikari to use for finding the Driver
        currentThread.setContextClassLoader(driverClassLoader);

        try {
            loadDataSource();
        } catch (MariaDB11Exception e) {
            // Try to set up again using MariaDB driver
            driverClassLoader = null;
            dataSource = null;
            useMariaDbDriver = true;
            loadDataSource();
        }

        // Reset the context classloader back to what it was originally set to, now that the DataSource is created
        currentThread.setContextClassLoader(previousClassLoader);
    }

    private void loadDataSource() {
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
            hikariConfig.setDriverClassName(useMariaDbDriver ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver");
            String protocol = useMariaDbDriver ? "jdbc:mariadb" : "jdbc:mysql";
            hikariConfig.setJdbcUrl(protocol + "://" + host + ":" + port + "/" + database + launchOptions);

            String username = config.get(DatabaseSettings.MYSQL_USER);
            String password = config.get(DatabaseSettings.MYSQL_PASS);

            hikariConfig.setUsername(username);
            hikariConfig.setPassword(password);
            hikariConfig.addDataSourceProperty("connectionInitSql", "set time_zone = '+00:00'");

            hikariConfig.setPoolName("Plan Connection Pool-" + increment);
            increment();

            hikariConfig.setAutoCommit(false);
            setMaxConnections(hikariConfig);
            hikariConfig.setMaxLifetime(config.get(DatabaseSettings.MAX_LIFETIME));
            hikariConfig.setLeakDetectionThreshold(config.get(DatabaseSettings.MAX_LIFETIME) + TimeUnit.SECONDS.toMillis(4L));

            this.dataSource = new HikariDataSource(hikariConfig);
        } catch (HikariPool.PoolInitializationException e) {
            if (e.getMessage().contains("Unknown system variable 'transaction_isolation'")) {
                throw new MariaDB11Exception("MySQL driver is incompatible with database that is being used.", e);
            }
            throw new DBInitException("Failed to set-up HikariCP Datasource: " + e.getMessage(), e);
        } finally {
            unloadMySQLDriver();
        }

        if (useMariaDbDriver) {
            checkMariaDBVersionIncompatibility();
        }
    }

    private void checkMariaDBVersionIncompatibility() {
        executeTransaction(new OperationCriticalTransaction() {
            @Override
            protected void performOperations() {
                query(MySQLSchemaQueries.getVersion())
                        .filter("11.0.2-MariaDB"::equals)
                        .ifPresent(badVersion -> {
                            throw new DBInitException("MariaDB version " + badVersion + " inserts incorrect data due to a bug in query execution order so it is not supported. Upgrade MariaDB to 11.1.1 or newer, or downgrade to MariaDB 10.");
                        });
            }
        });
    }

    private void setMaxConnections(HikariConfig hikariConfig) {
        try {
            hikariConfig.setMaximumPoolSize(config.get(DatabaseSettings.MAX_CONNECTIONS));
        } catch (IllegalStateException e) {
            logger.warn(e.getMessage() + ", using 1 as maximum for now.");
            hikariConfig.setMaximumPoolSize(1);
        }
    }

    private void unloadMySQLDriver() {
        // Avoid issues with other plugins by removing the mysql driver from driver manager
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            Class<?> driverClass = driver.getClass();
            // Checks that it's from our class loader to avoid unloading another plugin's/the server's driver
            String driverName = useMariaDbDriver ? "org.mariadb.jdbc.Driver" : "com.mysql.cj.jdbc.Driver";
            if (driverName.equals(driverClass.getName()) && driverClass.getClassLoader() == driverClassLoader) {
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
        setTimezoneToUTC(connection);
        return connection;
    }

    private void setTimezoneToUTC(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("set time_zone = '+00:00'");
        }
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
            errorLogger.error(e, ErrorContext.builder().related("Closing connection").build());
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
