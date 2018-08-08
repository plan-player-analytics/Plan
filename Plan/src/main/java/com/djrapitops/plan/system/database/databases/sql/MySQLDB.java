package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

    private static int increment = 1;

    protected volatile DataSource dataSource;

    public MySQLDB(Supplier<Locale> locale) {
        super(locale);
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
            HikariConfig config = new HikariConfig();

            String host = Settings.DB_HOST.toString();
            String port = Integer.toString(Settings.DB_PORT.getNumber());
            String database = Settings.DB_DATABASE.toString();
            String launchOptions = Settings.DB_LAUNCH_OPTIONS.toString();
            if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
                launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
                Log.error(locale.get().getString(PluginLang.DB_MYSQL_LAUNCH_OPTIONS_FAIL, launchOptions));
            }
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + launchOptions);

            String username = Settings.DB_USER.toString();
            String password = Settings.DB_PASS.toString();

            config.setUsername(username);
            config.setPassword(password);

            config.setPoolName("Plan Connection Pool-" + increment);
            increment();

            config.setAutoCommit(true);
            config.setMaximumPoolSize(8);
            config.setMaxLifetime(25L * TimeAmount.MINUTE.ms());
            config.setLeakDetectionThreshold(10L * TimeAmount.MINUTE.ms());

            this.dataSource = new HikariDataSource(config);

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
            Log.toLog(this.getClass(), e);
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
