package main.java.com.djrapitops.plan.database.databases;

import main.java.com.djrapitops.plan.Plan;
import org.apache.commons.dbcp2.BasicDataSource;

import java.io.File;
import java.util.Collections;

/**
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
     * @param plugin
     * @param dbName
     */
    public SQLiteDB(Plan plugin, String dbName) {
        super(plugin, false);
        this.dbName = dbName;
    }

    /**
     * Setups the {@link BasicDataSource}
     */
    @Override
    public void setupDataSource() {
        dataSource = new BasicDataSource();

        String filePath = new File(plugin.getDataFolder(), dbName + ".db").getAbsolutePath();
        dataSource.setUrl("jdbc:sqlite:" + filePath);

        dataSource.setEnableAutoCommitOnReturn(false);
        dataSource.setDefaultAutoCommit(false);

        dataSource.setConnectionInitSqls(Collections.singletonList("PRAGMA JOURNAL_MODE=WAL"));
        dataSource.setMaxTotal(-1);
    }

    /**
     * @return the name of the Database
     */
    @Override
    public String getName() {
        return "SQLite";
    }

}
