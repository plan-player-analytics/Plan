package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.info.BungeeInfoSystem;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.BungeeConnectionSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Bungee InfoSystem.
 *
 * @author Rsl1122
 */
@Module
public class BungeeInfoSystemModule {

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

}