package com.djrapitops.plan.database.databases;

import com.djrapitops.plan.Plan;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDB extends SQLDB {

    private final Plan plugin;

    public SQLiteDB(Plan plugin) {
        super(plugin, false);

        this.plugin = plugin;
    }

    @Override
    public Connection getNewConnection() {
        try {
            Class.forName("org.sqlite.JDBC");

            return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), "database.db").getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }

    @Override
    public void getConfigDefaults(ConfigurationSection section) {

    }

    @Override
    public String getName() {
        return "SQLite";
    }
}
