package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.SpongeDBSystem;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Sponge specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class SpongeSuperClassBindingModule {

    @Provides
    @Singleton
    DBSystem provideSpongeDatabaseSystem(SpongeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    ConfigSystem provideSpongeConfigSystem(SpongeConfigSystem spongeConfigSystem) {
        return spongeConfigSystem;
    }

    @Provides
    @Singleton
    TaskSystem provideSpongeTaskSystem(SpongeTaskSystem spongeTaskSystem) {
        return spongeTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideSpongeListenerSystem(SpongeListenerSystem spongeListenerSystem) {
        return spongeListenerSystem;
    }

    @Provides
    @Singleton
    ImportSystem provideImportSystem() {
        return new EmptyImportSystem();
    }

}