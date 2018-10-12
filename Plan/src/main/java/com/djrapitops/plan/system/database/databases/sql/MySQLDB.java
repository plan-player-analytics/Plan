package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.store.containers.NetworkContainer;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
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

    protected volatile DataSource dataSource;

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

    /**
     * @return the name of the Database
     */
    @Override
    public String getName() {
        return "MySQL";
    }

    /**
     * Setups the {@link HikariDataSource}
     */
    @Override
    public void setupDataSource() throws DBInitException {
        try {
            HikariConfig hikariConfig = new HikariConfig();

            String host = config.getString(Settings.DB_HOST);
            String port = config.getString(Settings.DB_PORT);
            String database = config.getString(Settings.DB_DATABASE);
            String launchOptions = config.getString(Settings.DB_LAUNCH_OPTIONS);
            if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
                logger.error(locale.getString(PluginLang.DB_MYSQL_LAUNCH_OPTIONS_FAIL, launchOptions));
            }
            hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + launchOptions);

            String username = config.getString(Settings.DB_USER);
            String password = config.getString(Settings.DB_PASS);

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
    public Connection getConnection() throws SQLException {
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
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
        super.close();
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
    public boolean isUsingMySQL() {
        return true;
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
