package com.djrapitops.plan.database.databases;

import com.djrapitops.plan.Plan;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        ConfigurationSection config = getConfigSection();

        setUserName(config.getString("tables.users"));
        setLocationName(config.getString("tables.locations"));
        setNicknamesName(config.getString("tables.nicknames"));
        setGamemodetimesName(config.getString("tables.gamemodes"));
        setIpsName(config.getString("tables.ips"));
        setCommanduseName(config.getString("tables.commandusages"));
        setServerdataName(config.getString("tables.serverdata"));

        try {
            Class.forName("com.mysql.jdbc.Driver");

            String url = "jdbc:mysql://" + config.getString("host") + ":" + config.getString("port") + "/" + config.getString("database");

            return DriverManager.getConnection(url, config.getString("user"), config.getString("password"));
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }

    private ConfigurationSection getSection(ConfigurationSection parent, String childName) {
        ConfigurationSection child = parent.getConfigurationSection(childName);

        if (child == null) {
            child = parent.createSection(childName);
        }

        return child;
    }

    @Override
    public void getConfigDefaults(ConfigurationSection section) {
        section.addDefault("host", "localhost");
        section.addDefault("port", 3306);
        section.addDefault("user", "root");
        section.addDefault("password", "minecraft");
        section.addDefault("database", "Plan");

        ConfigurationSection tables = getSection(section, "tables");

        tables.addDefault("users", "plan_users");
        tables.addDefault("locations", "plan_locations");
        tables.addDefault("nicknames", "plan_nicknames");
        tables.addDefault("gamemodetimes", "plan_gamemodetimes");
        tables.addDefault("ips", "plan_ips");
        tables.addDefault("commandusages", "plan_commandusages");
        tables.addDefault("serverdata", "plan_serverdata");
    }

    @Override
    public String getName() {
        return "MySQL";
    }
}
