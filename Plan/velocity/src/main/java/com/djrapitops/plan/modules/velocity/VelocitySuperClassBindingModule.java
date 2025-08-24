/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.modules.velocity;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.VelocitySensor;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.gathering.listeners.VelocityListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.VelocityServerInfo;
import dagger.Binds;
import dagger.Module;

/**
 * Module for binding Velocity specific classes as interface implementations.
 *
 * @author AuroraLS3
 */
@Module
public interface VelocitySuperClassBindingModule {

    @Binds
    ServerInfo bindServerInfo(VelocityServerInfo serverInfo);

    @Binds
    ListenerSystem bindListenerSystem(VelocityListenerSystem listenerSystem);

    @Binds
    ServerSensor<?> bindServerSensor(VelocitySensor sensor);
}