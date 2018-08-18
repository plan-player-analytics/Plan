package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.ServerConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for InfoSystem.
 *
 * @author Rsl1122
 */
@Module
public class ServerInfoSystemModule {

    @Provides
    InfoSystem provideServerInfoSystem(ConnectionSystem connectionSystem) {
        return new ServerInfoSystem(connectionSystem);
    }

    @Provides
    ConnectionSystem provideServerConnectionSystem(Locale locale) {
        return new ServerConnectionSystem(() -> locale); // TODO Remove supplier
    }

}