/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems;

import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class DatabaseSystem implements SubSystem {

    private Database db;
    private Set<Database> databases;

    @Override
    public void init() throws PlanEnableException {
        try {
            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();
            Log.info(Locale.get(Msg.ENABLE_DB_INFO).parse(db.getConfigName()));
            Benchmark.stop("Systems", "Init Database");
        } catch (DatabaseInitException e) {
            throw new PlanEnableException(db.getConfigName() + "-Database failed to initialize", e);
        }
    }

    private void initDatabase() throws DatabaseInitException {
        if (Check.isBungeeAvailable()) {
            db = new MySQLDB();
        } else {
            // Enables database on Bukkit
            // Bukkit supports SQLite.
            initPlanDatabase();
        }
        db.init();
    }

    private void initPlanDatabase() throws DatabaseInitException {
        databases = new HashSet<>();
        databases.add(new MySQLDB());
        databases.add(new SQLiteDB());

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();
        db = getActiveDatabase(dbType);
    }

    public Set<Database> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<Database> databases) {
        this.databases = databases;
    }

    @Override
    public void close() {
        try {
            if (db != null) {
                db.close();
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public static DatabaseSystem getInstance() {
        return Systems.getInstance().databaseSystem;
    }

    public Database getActiveDatabase() {
        return db;
    }

    public static Database getActiveDatabase(String dbName) throws DatabaseInitException {
        for (Database database : DatabaseSystem.getInstance().getDatabases()) {
            String dbConfigName = database.getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                database.init();
                return database;
            }
        }
        throw new DatabaseInitException(Locale.get(Msg.ENABLE_FAIL_WRONG_DB) + " " + dbName);
    }
}