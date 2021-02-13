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
package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.identification.properties.BungeeServerProperties;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.settings.config.PlanConfig;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee ServerProperties.
 *
 * @author AuroraLS3
 */
@Module
public class BungeeServerPropertiesModule {

    @Provides
    @Singleton
    ServerProperties provideServerProperties(PlanBungee plugin, PlanConfig config) {
        return new BungeeServerProperties(plugin.getProxy(), config);
    }
}