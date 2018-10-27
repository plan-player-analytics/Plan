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