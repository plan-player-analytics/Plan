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
package net.playeranalytics.plan.module;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.importing.importers.Importer;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.MySQLDB;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.file.JarResource;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import dev.vankka.dependencydownload.path.DependencyPathProvider;
import net.playeranalytics.plan.gathering.NoOpListenerSystem;
import net.playeranalytics.plan.gathering.NoOpServerSensor;
import net.playeranalytics.plugin.PluginInformation;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author AuroraLS3
 */
@Module
public class StandaloneProvidingModule {

    @Provides
    @Singleton
    DBSystem provideDatabaseSystem(
            PlanConfig config,
            Locale locale,
            SQLiteDB.Factory sqLiteDB,
            MySQLDB mySQLDB,
            PluginLogger logger
    ) {
        return new DBSystem(config, locale, sqLiteDB, logger) {
            @Override
            public void enable() {
                databases.add(mySQLDB);
                db = getActiveDatabaseByName(DBType.MYSQL.getConfigName());
                super.enable();
            }
        };
    }

    @Provides
    @ElementsIntoSet
    Set<Importer> provideEmptyImporterSet() {
        return new HashSet<>();
    }

    @Provides
    @ElementsIntoSet
    Set<TaskSystem.Task> provideEmptyTaskSet() {
        return new HashSet<>();
    }

    @Provides
    @Singleton
    ListenerSystem provideListenerSystem() {
        return new NoOpListenerSystem();
    }

    @Provides
    @Singleton
    ServerSensor<?> provideServerSensor() {
        return new NoOpServerSensor();
    }

    @Provides
    @Singleton
    @Named("mainCommandName")
    String provideMainCommandName() {
        return "plan";
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    Locale provideLocale(LocaleSystem localeSystem) {
        return localeSystem.getLocale();
    }

    @Provides
    @Singleton
    ExtensionSettings providePluginsConfigSection(PlanConfig config) {
        return config.getExtensionSettings();
    }

    @Provides
    @Singleton
    @Named("isExtensionEnabled")
    Predicate<String> provideExtensionEnabledConfigCheck(PlanConfig config) {
        return config.getExtensionSettings()::isEnabled;
    }

    @Provides
    @Singleton
    JarResource.StreamFunction provideJarStreamFunction(PluginInformation information) {
        return information::getResourceFromJar;
    }

    @Provides
    @Singleton
    @Named("dataFolder")
    File provideDataFolder(PluginInformation information) {
        return information.getDataFolder();
    }

    @Provides
    @Singleton
    ApplicationDependencyManager applicationDependencyManager(@Named("dataFolder") File dataFolder) {
        Path librariesDirectory = dataFolder.toPath().resolve("libraries");
        return new ApplicationDependencyManager(DependencyPathProvider.directory(librariesDirectory));
    }
}
