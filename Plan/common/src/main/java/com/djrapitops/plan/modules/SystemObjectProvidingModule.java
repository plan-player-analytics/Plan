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

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.settings.config.ExtensionSettings;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.file.JarResource;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.io.File;
import java.util.function.Predicate;

/**
 * Module for binding object instances found inside other systems.
 *
 * @author Rsl1122
 */
@Module
public class SystemObjectProvidingModule {

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
    JarResource.StreamFunction provideJarStreamFunction(PlanPlugin plugin) {
        return plugin::getResource;
    }

    @Provides
    @Singleton
    @Named("dataFolder")
    File provideDataFolder(PlanPlugin plugin) {
        return plugin.getDataFolder();
    }

}