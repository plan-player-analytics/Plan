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

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plugin.logging.console.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Nukkit Database system that initializes SQLite and MySQL database objects.
 *
 * @author Rsl1122
 */
@Singleton
public class NukkitDBSystem extends DBSystem {

    private final PlanConfig config;

    @Inject
    public NukkitDBSystem(
            Locale locale,
            MySQLDB mySQLDB,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2DB,
            PlanConfig config,
            PluginLogger logger
    ) {
        super(locale, sqLiteDB, h2DB, logger);
        this.config = config;

        databases.add(mySQLDB);
        databases.add(h2DB.usingDefaultFile());
        databases.add(sqLiteDB.usingDefaultFile());
    }

    @Override
    public void enable() {
        String dbType = config.get(DatabaseSettings.TYPE).toLowerCase().trim();
        db = getActiveDatabaseByName(dbType);
        super.enable();
    }
}
