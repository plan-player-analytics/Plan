/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashSet;
import java.util.Set;

/**
 * System that holds the active databases.
 *
 * @author Rsl1122
 */
public abstract class DBSystem implements SubSystem {

    protected Database db;
    protected Set<Database> databases;

    public DBSystem() {
        databases = new HashSet<>();
    }

    public static DBSystem getInstance() {
        DBSystem dbSystem = PlanSystem.getInstance().getDatabaseSystem();
        Verify.nullCheck(dbSystem, () -> new IllegalStateException("Database system was not initialized."));
        return dbSystem;
    }

    public static Database getActiveDatabaseByName(String dbName) throws DBInitException {
        for (Database database : getInstance().getDatabases()) {
            String dbConfigName = database.getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                database.init();
                return database;
            }
        }
        throw new DBInitException(Locale.get(Msg.ENABLE_FAIL_WRONG_DB) + " " + dbName);
    }

    @Override
    public void enable() throws EnableException {
        try {
            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();
            db.scheduleClean(1L);
            Log.info(Locale.get(Msg.ENABLE_DB_INFO).parse(db.getConfigName()));
            Benchmark.stop("Enable", "Init Database");
        } catch (DBInitException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? e.getMessage() : cause.getMessage();
            throw new EnableException((db != null ? db.getName() : "Database") + " init failure: " + message, cause);
        }
    }

    protected abstract void initDatabase() throws DBInitException;

    public Set<Database> getDatabases() {
        return databases;
    }

    @Override
    public void disable() {
        try {
            if (db != null) {
                db.close();
            }
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    public Database getActiveDatabase() {
        return db;
    }

    public void setActiveDatabase(Database db) throws DBException {
        this.db.close();
        this.db = db;
    }
}
