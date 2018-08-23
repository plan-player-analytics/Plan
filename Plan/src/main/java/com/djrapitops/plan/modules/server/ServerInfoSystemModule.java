package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.ServerConnectionSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Dagger module for Server InfoSystem.
 *
 * @author Rsl1122
 */
@Module
public class ServerInfoSystemModule {

    @Provides
    @Singleton
    InfoSystem provideServerInfoSystem(ServerInfoSystem serverInfoSystem) {
        return serverInfoSystem;
    }

    @Provides
    @Singleton
    ConnectionSystem provideServerConnectionSystem(ServerConnectionSystem serverConnectionSystem) {
        return serverConnectionSystem;
    }

}