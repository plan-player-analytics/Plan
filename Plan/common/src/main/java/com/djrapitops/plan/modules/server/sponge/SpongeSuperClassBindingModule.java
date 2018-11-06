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
package com.djrapitops.plan.modules.server.sponge;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.SpongeDBSystem;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerServerInfo;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.settings.config.SpongeConfigSystem;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Sponge specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class SpongeSuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideSpongeServerInfo(ServerServerInfo serverServerInfo) {
        return serverServerInfo;
    }

    @Provides
    @Singleton
    DBSystem provideSpongeDatabaseSystem(SpongeDBSystem dbSystem) {
        return dbSystem;
    }

    @Provides
    @Singleton
    ConfigSystem provideSpongeConfigSystem(SpongeConfigSystem spongeConfigSystem) {
        return spongeConfigSystem;
    }

    @Provides
    @Singleton
    TaskSystem provideSpongeTaskSystem(SpongeTaskSystem spongeTaskSystem) {
        return spongeTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideSpongeListenerSystem(SpongeListenerSystem spongeListenerSystem) {
        return spongeListenerSystem;
    }

    @Provides
    @Singleton
    ImportSystem provideImportSystem() {
        return new EmptyImportSystem();
    }

}