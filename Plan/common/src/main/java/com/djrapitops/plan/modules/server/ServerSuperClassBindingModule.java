/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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