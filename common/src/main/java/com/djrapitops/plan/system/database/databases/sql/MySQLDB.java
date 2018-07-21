package com.djrapitops.plan.system.database.databases.sql;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.utility.log.Log;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

    protected DataSource dataSource;

    public MySQLDB() {
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() throws DBInitException {
        BasicDataSource dataSource = new BasicDataSource();
        this.dataSource = dataSource;
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        String host = Settings.DB_HOST.toString();
        String port = Integer.toString(Settings.DB_PORT.getNumber());
        String database = Settings.DB_DATABASE.toString();
        String launchOptions = Settings.DB_LAUNCH_OPTIONS.toString();
        if (launchOptions.isEmpty() || !launchOptions.startsWith("?") || launchOptions.endsWith("&")) {
            Log.error("Launch Options were faulty, using default (?rewriteBatchedStatements=true&useSSL=false)");
            launchOptions = "?rewriteBatchedStatements=true&useSSL=false";
        }

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + launchOptions);

        String username = Settings.DB_USER.toString();
        String password = Settings.DB_PASS.toString();

        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setInitialSize(1);
        dataSource.setMaxTotal(8);
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
        try {
            if (dataSource instanceof BasicDataSource) {
                ((BasicDataSource) dataSource).close();
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass(), e);
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
