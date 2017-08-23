package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.Plan;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * @author Rsl1122
 */
public class MySQLDB extends SQLDB {

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public MySQLDB(Plan plugin) {
        super(plugin);
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() {
        FileConfiguration config = plugin.getConfig();

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        String host = config.getString("Database.MySQL.Host");
        String port = config.getString("Database.MySQL.Port");
        String database = config.getString("Database.MySQL.Database");

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true");

        String username = config.getString("Database.MySQL.User");
        String password = config.getString("Database.MySQL.Password");

        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setMaxTotal(-1);
    }

    /**
     * @return the name of the Database
     */
    @Override
    public String getName() {
        return "MySQL";
    }
}
