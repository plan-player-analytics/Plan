/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.database;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.H2DB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.PluginLang;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * System that holds the active databases.
 *
 * @author Rsl1122
 */
@Singleton
public abstract class DBSystem implements SubSystem {

    protected final Locale locale;
    private final SQLiteDB.Factory sqLiteFactory;
    private final H2DB.Factory h2Factory;
    protected final PluginLogger logger;
    protected final Timings timings;
    protected final ErrorHandler errorHandler;

    protected Database db;
    protected Set<Database> databases;

    public DBSystem(
            Locale locale,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2Factory,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.sqLiteFactory = sqLiteDB;
        this.h2Factory = h2Factory;
        this.logger = logger;
        this.timings = timings;
        this.errorHandler = errorHandler;
        databases = new HashSet<>();
    }

    public Database getActiveDatabaseByName(String dbName) {
        for (Database database : getDatabases()) {
            String dbConfigName = database.getType().getConfigName();
            if (Verify.equalsIgnoreCase(dbName, dbConfigName)) {
                return database;
            }
        }
        throw new IllegalArgumentException(locale.getString(PluginLang.ENABLE_FAIL_WRONG_DB, dbName));
    }

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
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    public Database getDatabase() {
        return db;
    }

    @Override
    public void enable() throws EnableException {
        try {
            db.init();
            db.scheduleClean(20L);
            logger.info(locale.getString(PluginLang.ENABLED_DATABASE, db.getType().getName()));
        } catch (DBInitException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? e.getMessage() : cause.getMessage();
            throw new EnableException((db != null ? db.getType().getName() : "Database") + " init failure: " + message, cause);
        }
    }

    public void setActiveDatabase(Database db) throws DBException {
        this.db.close();
        this.db = db;
    }

    public SQLiteDB.Factory getSqLiteFactory() {
        return sqLiteFactory;
    }

    public H2DB.Factory getH2Factory() {
        return h2Factory;
    }
}
