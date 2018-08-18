package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.ServerDBSystem;
import com.djrapitops.plan.system.locale.Locale;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for server database.
 *
 * @author Rsl1122
 */
@Module
public class ServerDatabaseModule {

    @Provides
    DBSystem provideDatabaseSystem(Locale locale) {
        return new ServerDBSystem(() -> locale);
    }

}