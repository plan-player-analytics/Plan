package com.djrapitops.plan.modules.bungee;

import com.djrapitops.plan.system.database.BungeeDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for bukkit database.
 *
 * @author Rsl1122
 */
@Module
public class BungeeDatabaseModule {

    @Provides
    DBSystem provideDatabaseSystem(BungeeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    Database provideDatabase(DBSystem dbSystem) {
        return dbSystem.getActiveDatabase();
    }

}