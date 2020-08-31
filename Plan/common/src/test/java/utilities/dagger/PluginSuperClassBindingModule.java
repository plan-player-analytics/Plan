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
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DatabaseSettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.H2DB;
import com.djrapitops.plan.storage.database.MySQLDB;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;
import utilities.mocks.TestProcessing;

import javax.inject.Named;
import javax.inject.Singleton;

import static org.mockito.Mockito.when;

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

    @Provides
    @Singleton
    TaskSystem provideTaskSystem(RunnableFactory runnableFactory) {
        return new TaskSystem(runnableFactory) {
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
    Processing provideProcessing(TestProcessing testProcessing) {
        return testProcessing;
    }

    @Provides
    @Singleton
    ServerSensor<?> provideServerSensor() {
        ServerSensor<?> mock = Mockito.mock(ServerSensor.class);
        when(mock.getWorlds()).thenCallRealMethod();
        when(mock.getChunkCount(Mockito.any())).thenCallRealMethod();
        when(mock.getEntityCount(Mockito.any())).thenCallRealMethod();
        when(mock.getTPS()).thenCallRealMethod();
        return mock;
    }

    @Provides
    @Singleton
    @Named("mainCommandName")
    String provideMainCommandName() {
        return "plan";
    }

}