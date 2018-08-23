package com.djrapitops.plan.modules.server.bukkit;

import com.djrapitops.plan.system.database.BukkitDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for bukkit database.
 *
 * @author Rsl1122
 */
@Module
public class BukkitDatabaseModule {

    @Provides
    @Singleton
    DBSystem provideDatabaseSystem(BukkitDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    Database provideDatabase(DBSystem dbSystem) {
        return dbSystem.getActiveDatabase();
    }

}