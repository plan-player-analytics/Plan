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

    private final String dbName;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SQLiteDB(Plan plugin) {
        this(plugin, "database");
    }
    
    /**
     *
     * @param plugin
     * @param dbName
     */
    public SQLiteDB(Plan plugin, String dbName) {
        super(plugin, false);
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
    
    /**
     *
     * @param dbName
     * @return
     */
    public Connection getNewConnection(String dbName) {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), dbName+".db").getAbsolutePath());
            
            return connection;
        } catch (ClassNotFoundException | SQLException e) {
            return null;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "SQLite";
    }
}
