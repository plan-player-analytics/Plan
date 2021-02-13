/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.storage.database;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Singleton;
import java.util.HashSet;
import java.util.Set;

/**
 * System that holds the active databases.
 *
 * @author AuroraLS3
 */
@Singleton
public class DBSystem implements SubSystem {

    protected final Locale locale;
    private final SQLiteDB.Factory sqLiteFactory;
    private final H2DB.Factory h2Factory;
    protected final PluginLogger logger;

    protected Database db;
    protected final Set<Database> databases;

    public DBSystem(
            Locale locale,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2Factory,
            PluginLogger logger
    ) {
        this.locale = locale;
        this.sqLiteFactory = sqLiteDB;
        this.h2Factory = h2Factory;
        this.logger = logger;
        databases = new HashSet<>();
    }

    public Database getActiveDatabaseByName(String dbName) {
        return DBType.getForName(dbName)
                .map(this::getActiveDatabaseByType)
                .orElseThrow(() -> new IllegalArgumentException(locale.getString(PluginLang.ENABLE_FAIL_WRONG_DB, dbName)));
    }

    public Database getActiveDatabaseByType(DBType type) {
        for (Database database : getDatabases()) {
            if (database.getType() == type) {
                return database;
            }
        }
        throw new IllegalArgumentException(locale.getString(PluginLang.ENABLE_FAIL_WRONG_DB, type != null ? type.getName() : "null"));
    }

    public Set<Database> getDatabases() {
        return databases;
    }

    @Override
    public void disable() {
        if (db != null) {
            db.close();
        }
    }

    public Database getDatabase() {
        return db;
    }

    @Override
    public void enable() {
        try {
            db.init();
            logger.info(locale.getString(PluginLang.ENABLED_DATABASE, db.getType().getName()));
        } catch (DBInitException e) {
            Throwable cause = e.getCause();
            String message = cause == null ? e.getMessage() : cause.getMessage();
            throw new EnableException(db.getType().getName() + " init failure: " + message, cause);
        }
    }

    public void setActiveDatabase(Database db) {
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
