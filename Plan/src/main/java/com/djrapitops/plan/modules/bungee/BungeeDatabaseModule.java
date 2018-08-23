package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for bungee database.
 *
 * @author Rsl1122
 */
@Module
public class BungeeDatabaseModule {

    @Provides
    @Singleton
    DBSystem provideDatabaseSystem(BungeeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    Database provideDatabase(DBSystem dbSystem) {
        return dbSystem.getActiveDatabase();
    }

}