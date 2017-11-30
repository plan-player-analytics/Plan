package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.api.IPlan;
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
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public MySQLDB(IPlan plugin) {
        super(plugin);
    }

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

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true&useSSL=false");

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
