package main.java.com.djrapitops.plan.database.databases;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Plan;

/**
 *
 * @author Rsl1122
 */
public class SQLiteDB extends SQLDB {

    private final Plan plugin;
    private final String dbName;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SQLiteDB(Plan plugin) {
        this(plugin, "database");
    }
    
    public SQLiteDB(Plan plugin, String dbName) {
        super(plugin, false);

        this.plugin = plugin;
        this.dbName = dbName;
    }

    /**
     * Creates a new connection to the database.
     *
     * @return the new Connection.
     */
    @Override
    public Connection getNewConnection() {
        return getNewConnection(dbName);
    }
    
    public Connection getNewConnection(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");

            return DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), dbName+".db").getAbsolutePath());
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return "SQLite";
    }
}
