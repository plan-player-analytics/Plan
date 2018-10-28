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
package com.djrapitops.pluginbridge.plan;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger modules for different plugin bridges.
 *
 * @author Rsl1122
 */
public class PluginBridgeModule {

    @Module
    public static class Bukkit {
        @Provides
        @Singleton
        public Bridge provideBridge(BukkitBridge bridge) {
            return bridge;
        }
    }

    @Module
    public static class Bungee {
        @Provides
        @Singleton
        public Bridge provideBridge(BungeeBridge bridge) {
            return bridge;
        }
    }

    @Module
    public static class Sponge {
        @Provides
        @Singleton
        public Bridge provideBridge(SpongeBridge bridge) {
            return bridge;
        }
    }

    @Module
    public static class Velocity {
        @Provides
        @Singleton
        public Bridge provideBridge(VelocityBridge bridge) {
            return bridge;
        }
    }
}