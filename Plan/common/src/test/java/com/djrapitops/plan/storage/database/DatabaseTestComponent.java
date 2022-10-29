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

import com.djrapitops.plan.component.ComponentSvc;
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.modules.FiltersModule;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.storage.database.queries.filter.QueryFilters;
import com.djrapitops.plan.storage.file.PlanFiles;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import utilities.DBPreparer;
import utilities.TestPluginLogger;
import utilities.dagger.*;
import utilities.mocks.objects.TestRunnableFactory;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;

@Singleton
@Component(modules = {
        DBSystemModule.class,
        TestSystemObjectProvidingModule.class,
        FiltersModule.class,

        DatabaseTestComponent.DBTestModule.class,
        PlanPluginModule.class,
        PlanServerPluginModule.class,
        PluginServerPropertiesModule.class,
        PluginSuperClassBindingModule.class
})
public interface DatabaseTestComponent extends DBPreparer.Dependencies {

    default void enable() {
        SQLDB.setDownloadDriver(false);

        files().enable();
        configSystem().enable();
        dbSystem().enable();
        serverInfo().enable();
    }

    default void disable() {
        serverInfo().disable();
        dbSystem().disable();
        configSystem().disable();
        files().disable();
    }

    PlanConfig config();

    ConfigSystem configSystem();

    DBSystem dbSystem();

    ServerInfo serverInfo();

    DeliveryUtilities deliveryUtilities();

    PlanFiles files();

    ExtensionSvc extensionService();

    ComponentSvc componentService();

    QueryFilters queryFilters();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder bindTemporaryDirectory(@Named("tempDir") Path tempDir);

        DatabaseTestComponent build();
    }

    @Module
    class DBTestModule {
        @Provides
        @Singleton
        PluginLogger provideLogger() {
            return new TestPluginLogger();
        }

        @Provides
        @Singleton
        RunnableFactory provideRunnableFactory() {
            return new TestRunnableFactory();
        }

        @Provides
        @Singleton
        @Named("currentVersion")
        String provideCurrentVersion() {
            return "1.0.0";
        }
    }

}
