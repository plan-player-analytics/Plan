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
import com.djrapitops.plan.commands.use.ColorScheme;
import dagger.Module;
import dagger.Provides;
import net.playeranalytics.plugin.PlatformAbstractionLayer;
import net.playeranalytics.plugin.PluginInformation;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Dagger module for defining Platform Abstraction Layer utilities.
 *
 * @author AuroraLS3
 */
@Module
public class PlatformAbstractionLayerModule {

    @Provides
    @Named("currentVersion")
    @Singleton
    String provideCurrentVersion(PluginInformation pluginInformation) {
        return pluginInformation.getVersion();
    }

    @Provides
    @Singleton
    ColorScheme provideColorScheme(PlanPlugin plugin) {
        return plugin.getColorScheme();
    }

    @Provides
    @Singleton
    PluginLogger providePluginLogger(PlatformAbstractionLayer abstractionLayer) {
        return abstractionLayer.getPluginLogger();
    }

    @Provides
    @Singleton
    RunnableFactory provideRunnableFactory(PlatformAbstractionLayer abstractionLayer) {
        return abstractionLayer.getRunnableFactory();
    }

    @Provides
    @Singleton
    Listeners provideListeners(PlatformAbstractionLayer abstractionLayer) {
        return abstractionLayer.getListeners();
    }

    @Provides
    @Singleton
    PluginInformation providePluginInformation(PlatformAbstractionLayer abstractionLayer) {
        return abstractionLayer.getPluginInformation();
    }
}
