package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plan.system.settings.Settings;
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

    protected DataSource dataSource;

    public MySQLDB(Supplier<Locale> locale) {
        super(locale);
    }

    /**
     * Setups the {@link HikariDataSource}
     */
    @Override
    public void setupDataSource() throws DBInitException {
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

        config.setPoolName("Plan Connection Pool");
        config.setDriverClassName("com.mysql.jdbc.Driver");

        config.setAutoCommit(true);
        config.setReadOnly(false);
        config.setMaximumPoolSize(8);

        this.dataSource = new HikariDataSource(config);
    }

    /**
     * @return the name of the Database
     */
    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
        super.close();
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
