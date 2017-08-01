package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
     * Creates a new connection to the database.
     *
     * @return the new Connection.
     */
    @Override
    public Connection getNewConnection() {
        FileConfiguration config = plugin.getConfig();

        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database");

            return DriverManager.getConnection(url, config.getString("mysql.user"), config.getString("mysql.password"));
        } catch (ClassNotFoundException | SQLException e) {
            Log.error(Phrase.DB_CONNECTION_FAIL.parse(getConfigName(), e.getMessage()));
            return null;
        }
    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return "MySQL";
    }
}
