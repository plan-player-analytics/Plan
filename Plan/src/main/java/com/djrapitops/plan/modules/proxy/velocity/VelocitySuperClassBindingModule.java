package com.djrapitops.plan.modules.proxy.bungee;

import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.BungeeListenerSystem;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.settings.config.BungeeConfigSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.BungeeTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Bungee specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class BungeeSuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideBungeeServerInfo(BungeeServerInfo bungeeServerInfo) {
        return bungeeServerInfo;
    }

    @Provides
    @Singleton
    ConfigSystem provideBungeeConfigSystem(BungeeConfigSystem bungeeConfigSystem) {
        return bungeeConfigSystem;
    }

    @Provides
    @Singleton
    TaskSystem provideBungeeTaskSystem(BungeeTaskSystem bungeeTaskSystem) {
        return bungeeTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideBungeeListenerSystem(BungeeListenerSystem bungeeListenerSystem) {
        return bungeeListenerSystem;
    }
}