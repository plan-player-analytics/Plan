package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

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
}
