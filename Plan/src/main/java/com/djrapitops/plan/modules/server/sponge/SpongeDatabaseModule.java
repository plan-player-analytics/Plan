package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.SpongeDBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for bukkit database.
 *
 * @author Rsl1122
 */
@Module
public class SpongeDatabaseModule {

    @Provides
    DBSystem provideDatabaseSystem(SpongeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    Database provideDatabase(DBSystem dbSystem) {
        return dbSystem.getActiveDatabase();
    }

}