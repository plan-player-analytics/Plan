package main.java.com.djrapitops.plan.database.databases;

import com.djrapitops.plugin.config.fileconfig.IFileConfig;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;

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
        IFileConfig config;
        try {
            config = plugin.getIConfig().getConfig();
        } catch (IOException e) {
            throw new DatabaseInitException("Failed to read config.", e);
        }

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        String host = config.getString("Database.MySQL.Host");
        String port = config.getInt("Database.MySQL.Port").toString();
        String database = config.getString("Database.MySQL.Database");

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true");

        String username = config.getString("Database.MySQL.User");
        String password = config.getString("Database.MySQL.Password");

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
