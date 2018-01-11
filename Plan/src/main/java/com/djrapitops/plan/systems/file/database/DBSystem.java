/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.systems.file.database;

import com.djrapitops.plan.api.exceptions.DatabaseInitException;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class DBSystem implements SubSystem {

    protected SQLDB db;
    protected Set<SQLDB> databases;

    public DBSystem() {
        databases = new HashSet<>();
    }

    public static DBSystem getInstance() {
        return Systems.getInstance().getDatabaseSystem();
    }

    @Override
    public void enable() throws EnableException {
        try {
            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();
            db.scheduleClean(10L);
            Log.info(Locale.get(Msg.ENABLE_DB_INFO).parse(db.getConfigName()));
            Benchmark.stop("Systems", "Init Database");
        } catch (DatabaseInitException e) {
            throw new EnableException(db.getName() + "-Database failed to initialize", e);
        }
    }

    protected abstract void initDatabase() throws DatabaseInitException;

    public Set<SQLDB> getDatabases() {
        return databases;
    }

    public void setDatabases(Set<SQLDB> databases) {
        this.databases = databases;
    }

    @Override
    public void disable() {
        try {
            if (db != null) {
                db.close();
            }
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public Database getActiveDatabase() {
        return db;
    }

    public static SQLDB getActiveDatabase(String dbName) throws DatabaseInitException {
        for (SQLDB database : DBSystem.getInstance().getDatabases()) {
            String dbConfigName = database.getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                database.init();
                return database;
            }
        }
        throw new DatabaseInitException(Locale.get(Msg.ENABLE_FAIL_WRONG_DB) + " " + dbName);
    }
}