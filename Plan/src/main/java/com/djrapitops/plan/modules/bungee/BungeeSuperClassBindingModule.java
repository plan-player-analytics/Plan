package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.api.BungeeAPI;
import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.BungeeInfoSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.BungeeConnectionSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
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
    PlanAPI provideBungeePlanAPI(BungeeAPI bungeeAPI) {
        return bungeeAPI;
    }

    @Provides
    @Singleton
    DBSystem provideBungeeDatabaseSystem(BungeeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    ConfigSystem provideBungeeConfigSystem(BungeeConfigSystem bungeeConfigSystem) {
        return bungeeConfigSystem;
    }

    @Provides
    @Singleton
    InfoSystem provideBungeeInfoSystem(BungeeInfoSystem bungeeInfoSystem) {
        return bungeeInfoSystem;
    }

    @Provides
    @Singleton
    ConnectionSystem provideBungeeConnectionSystem(BungeeConnectionSystem bungeeConnectionSystem) {
        return bungeeConnectionSystem;
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

    @Provides
    @Singleton
    ImportSystem provideImportSystem() {
        return new EmptyImportSystem();
    }

}