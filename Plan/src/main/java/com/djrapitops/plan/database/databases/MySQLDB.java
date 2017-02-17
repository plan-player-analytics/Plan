package main.java.com.djrapitops.plan.database.databases;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.configuration.file.FileConfiguration;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
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
     * Creates a new connection to the database.
     *
     * @return the new Connection.
     */
    @Override
    protected Connection getNewConnection() {
        FileConfiguration config = getPlugin(Plan.class).getConfig();
        
        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database");
            Connection connection = DriverManager.getConnection(url, config.getString("mysql.user"), config.getString("mysql.password"));
            
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            getPlugin(Plan.class).logError(Phrase.DB_CONNECTION_FAIL.parse(getConfigName(), e.getMessage()));
            return null;
        }
    }

    @Override
    public String getName() {
        return "MySQL";
    }
}
