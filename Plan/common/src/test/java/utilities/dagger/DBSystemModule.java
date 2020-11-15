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
package utilities.dagger;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.H2DB;
import com.djrapitops.plan.storage.database.MySQLDB;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plugin.logging.console.PluginLogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DBSystemModule {
    @Provides
    @Singleton
    DBSystem provideDatabaseSystem(
            PlanConfig config,
            Locale locale,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2Factory,
            MySQLDB mySQLDB,
            PluginLogger logger
    ) {
        return new DBSystem(locale, sqLiteDB, h2Factory, logger) {
            @Override
            public void enable() throws EnableException {
                databases.add(sqLiteDB.usingDefaultFile());
                databases.add(h2Factory.usingDefaultFile());
                databases.add(mySQLDB);
                String dbType = config.get(DatabaseSettings.TYPE).toLowerCase().trim();
                db = getActiveDatabaseByName(dbType);
                super.enable();
            }
        };
    }
}
