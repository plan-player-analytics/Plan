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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.sql.H2DB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DatabaseSettings;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.pluginbridge.plan.Bridge;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

import javax.inject.Singleton;

/**
 * Module for binding Bukkit specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class PluginSuperClassBindingModule {

    @Provides
    @Singleton
    DBSystem provideDatabaseSystem(
            PlanConfig config,
            Locale locale,
            SQLiteDB.Factory sqLiteDB,
            H2DB.Factory h2Factory,
            PluginLogger logger,
            Timings timings,
            ErrorHandler errorHandler
    ) {
        return new DBSystem(locale, sqLiteDB, h2Factory, logger, timings, errorHandler) {
            @Override
            public void enable() throws EnableException {
                databases.add(sqLiteDB.usingDefaultFile());
                String dbType = config.get(DatabaseSettings.TYPE).toLowerCase().trim();
                db = getActiveDatabaseByName(dbType);
                super.enable();
            }
        };
    }

    @Provides
    @Singleton
    TaskSystem provideTaskSystem(RunnableFactory runnableFactory) {
        return new TaskSystem(runnableFactory, null) {
            @Override
            public void enable() {
            }
        };
    }

    @Provides
    @Singleton
    ListenerSystem provideListenerSystem() {
        return new ListenerSystem() {
            @Override
            protected void registerListeners() {
            }

            @Override
            protected void unregisterListeners() {
            }

            @Override
            public void callEnableEvent(PlanPlugin plugin) {
            }
        };
    }

    @Provides
    @Singleton
    Bridge providePluginBridge() {
        return Mockito.mock(Bridge.class);
    }

}