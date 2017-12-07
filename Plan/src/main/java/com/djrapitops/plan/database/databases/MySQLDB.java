package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.settings.Settings;
import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

    private BasicDataSource dataSource;

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() throws DatabaseInitException {
        dataSource = new BasicDataSource();
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
    public void close() throws SQLException {
        dataSource.close();
        super.close();
    }
}
