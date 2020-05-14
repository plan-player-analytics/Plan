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
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for defining Abstract Plugin Framework utilities.
 *
 * @author Rsl1122
 */
@Module
public class APFModule {
    @Provides
    @Singleton
    IPlugin provideIPlugin(PlanPlugin plugin) {
        return plugin;
    }

    @Provides
    @Named("currentVersion")
    @Singleton
    String provideCurrentVersion(IPlugin plugin) {
        return plugin.getVersion().replace("%buildNumber%", "?");
    }

    @Provides
    @Singleton
    ColorScheme provideColorScheme(PlanPlugin plugin) {
        return plugin.getColorScheme();
    }

    @Provides
    @Singleton
    DebugLogger provideDebugLogger(IPlugin plugin) {
        return plugin.getDebugLogger();
    }

    @Provides
    @Singleton
    PluginLogger providePluginLogger(IPlugin plugin) {
        return plugin.getPluginLogger();
    }

    @Provides
    @Singleton
    ErrorHandler provideErrorHandler(ErrorLogger errorLogger) {
        return errorLogger;
    }

    @Provides
    @Singleton
    Timings provideTimings(IPlugin plugin) {
        return plugin.getTimings();
    }

    @Provides
    @Singleton
    RunnableFactory provideRunnableFactory(IPlugin plugin) {
        return plugin.getRunnableFactory();
    }

}