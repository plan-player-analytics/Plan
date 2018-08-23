package com.djrapitops.plan.modules.server;

import com.djrapitops.plan.api.PlanAPI;
import com.djrapitops.plan.api.ServerAPI;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.ServerInfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.info.connection.ServerConnectionSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Server specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class ServerSuperClassBindingModule {

    @Provides
    @Singleton
    PlanAPI provideServerPlanAPI(ServerAPI serverAPI) {
        return serverAPI;
    }

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