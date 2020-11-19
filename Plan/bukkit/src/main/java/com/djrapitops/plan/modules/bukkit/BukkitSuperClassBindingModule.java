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
package com.djrapitops.plan.modules.bukkit;

import com.djrapitops.plan.BukkitServerShutdownSave;
import com.djrapitops.plan.BukkitTaskSystem;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.BukkitSensor;
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.listeners.BukkitListenerSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.BukkitConfigSystem;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.storage.database.BukkitDBSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import dagger.Binds;
import dagger.Module;
import org.bukkit.World;

/**
 * Module for binding Bukkit specific classes as interface implementations.
 *
 * @author Rsl1122
 */
@Module
public interface BukkitSuperClassBindingModule {

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverInfo);

    @Binds
    DBSystem bindDBSystem(BukkitDBSystem dbSystem);

    @Binds
    ConfigSystem bindConfigSystem(BukkitConfigSystem configSystem);

    @Binds
    TaskSystem bindTaskSystem(BukkitTaskSystem taskSystem);

    @Binds
    ListenerSystem bindListenerSystem(BukkitListenerSystem listenerSystem);

    @Binds
    ServerShutdownSave bindServerShutdownSave(BukkitServerShutdownSave shutdownSave);

    @Binds
    ServerSensor<World> bindServerSensor(BukkitSensor sensor);

    @Binds
    ServerSensor<?> bindGenericsServerSensor(ServerSensor<World> sensor);
}