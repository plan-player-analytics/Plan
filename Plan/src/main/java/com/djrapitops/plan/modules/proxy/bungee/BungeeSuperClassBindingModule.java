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
package com.djrapitops.plan.modules.proxy.bungee;

import com.djrapitops.plan.system.info.server.BungeeServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.listeners.BungeeListenerSystem;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.tasks.BungeeTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Bungee specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class BungeeSuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideBungeeServerInfo(BungeeServerInfo bungeeServerInfo) {
        return bungeeServerInfo;
    }

    @Provides
    @Singleton
    TaskSystem provideBungeeTaskSystem(BungeeTaskSystem bungeeTaskSystem) {
        return bungeeTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideBungeeListenerSystem(BungeeListenerSystem bungeeListenerSystem) {
        return bungeeListenerSystem;
    }
}