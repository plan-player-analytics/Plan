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
package com.djrapitops.plan.modules.proxy.velocity;

import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.VelocityServerInfo;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.listeners.VelocityListenerSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.tasks.VelocityTaskSystem;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * Module for binding Velocity specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public class VelocitySuperClassBindingModule {

    @Provides
    @Singleton
    ServerInfo provideVelocityServerInfo(VelocityServerInfo velocityServerInfo) {
        return velocityServerInfo;
    }

    @Provides
    @Singleton
    TaskSystem provideVelocityTaskSystem(VelocityTaskSystem velocityTaskSystem) {
        return velocityTaskSystem;
    }

    @Provides
    @Singleton
    ListenerSystem provideVelocityListenerSystem(VelocityListenerSystem velocityListenerSystem) {
        return velocityListenerSystem;
    }
}