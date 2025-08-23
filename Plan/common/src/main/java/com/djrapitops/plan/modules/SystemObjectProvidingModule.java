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
package com.djrapitops.plan.modules;

import com.djrapitops.plan.DataService;
import com.djrapitops.plan.DataSvc;
import com.djrapitops.plan.delivery.webserver.cache.JSONFileStorage;
import com.djrapitops.plan.delivery.webserver.cache.JSONMemoryStorageShim;
import com.djrapitops.plan.delivery.webserver.cache.JSONStorage;
import com.djrapitops.plan.delivery.webserver.http.JettyWebserver;
import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.gathering.importing.importers.Importer;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.file.JarResource;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.utilities.logging.PluginErrorLogger;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import dev.vankka.dependencydownload.path.DependencyPathProvider;
import net.playeranalytics.plugin.PluginInformation;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Module for binding object instances found inside other systems.
 *
 * @author AuroraLS3
 */
@Module
public class SystemObjectProvidingModule {

    @Provides
    @Singleton
    WebServer provideWebserver(JettyWebserver webServer) {
        return webServer;
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @ElementsIntoSet
    Set<Importer> emptyImporterSet() {
        return new HashSet<>();
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
    JarResource.StreamFunction provideJarStreamFunction(PluginInformation pluginInformation) {
        return pluginInformation::getResourceFromJar;
    }

    @Provides
    @Singleton
    @Named("dataFolder")
    File provideDataFolder(PluginInformation pluginInformation) {
        return pluginInformation.getDataFolder();
    }

    @Provides
    @Singleton
    ErrorLogger provideErrorLogger(PluginErrorLogger errorLogger) {
        return errorLogger;
    }

    @Provides
    @Singleton
    DataService provideDataService(DataSvc dataService) {
        return dataService;
    }

    @Provides
    @Singleton
    JSONStorage provideJSONStorage(
            PlanConfig config,
            JSONFileStorage jsonFileStorage
    ) {
        return new JSONMemoryStorageShim(config, jsonFileStorage);
    }

    @Provides
    @Singleton
    ApplicationDependencyManager applicationDependencyManager(@Named("dataFolder") File dataFolder) {
        Path librariesDirectory = dataFolder.toPath().resolve("libraries");
        return new ApplicationDependencyManager(DependencyPathProvider.directory(librariesDirectory));
    }

}