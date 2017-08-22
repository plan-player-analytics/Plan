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
        super(plugin, true);
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() {
        FileConfiguration config = plugin.getConfig();

        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");

        dataSource.setUrl("jdbc:mysql://" + config.getString("Database.MySQL.Host") + ":" + config.getString("Database.MySQL.Port") + "/"
                + config.getString("Database.MySQL.Database")
                + "?rewriteBatchedStatements=true");

        dataSource.setUsername(config.getString("Database.MySQL.User"));
        dataSource.setPassword(config.getString("Database.MySQL.Password"));
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
