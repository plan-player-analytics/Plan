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
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * System that holds the active databases.
 *
 * @author Rsl1122
 */
public abstract class DBSystem implements SubSystem {

    protected final Supplier<Locale> locale;

    protected Database db;
    protected Set<Database> databases;

    public DBSystem(Supplier<Locale> locale) {
        this.locale = locale;
        databases = new HashSet<>();
    }

    public static DBSystem getInstance() {
        DBSystem dbSystem = PlanSystem.getInstance().getDatabaseSystem();
        Verify.nullCheck(dbSystem, () -> new IllegalStateException("Database system was not initialized."));
        return dbSystem;
    }

    public static Database getActiveDatabaseByName(String dbName) throws DBInitException {
        DBSystem system = getInstance();
        for (Database database : system.getDatabases()) {
            String dbConfigName = database.getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                database.init();
                return database;
            }
        }
        throw new DBInitException(system.locale.get().getString(PluginLang.ENABLE_FAIL_WRONG_DB, dbName));
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

    @Override
    public void enable() throws EnableException {
        try {
            Benchmark.start("Init Database");
            initDatabase();
            db.scheduleClean(20L);
            Log.info(locale.get().getString(PluginLang.ENABLED_DATABASE, db.getName()));
            Benchmark.stop("Enable", "Init Database");
        } catch (DBInitException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? e.getMessage() : cause.getMessage();
            throw new EnableException((db != null ? db.getName() : "Database") + " init failure: " + message, cause);
        }
    }

    public void setActiveDatabase(Database db) throws DBException {
        this.db.close();
        this.db = db;
    }
}
