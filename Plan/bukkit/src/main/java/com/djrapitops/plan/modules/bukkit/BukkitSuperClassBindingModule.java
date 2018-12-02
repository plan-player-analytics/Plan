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

import com.djrapitops.plan.system.database.BukkitDBSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.importing.BukkitImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerServerInfo;
import com.djrapitops.plan.system.listeners.BukkitListenerSystem;
import com.djrapitops.plan.system.listeners.ListenerSystem;
import com.djrapitops.plan.system.settings.config.BukkitConfigSystem;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.BukkitTaskSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import dagger.Binds;
import dagger.Module;

/**
 * Module for binding Bukkit specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public interface BukkitSuperClassBindingModule {

    @Binds
    ServerInfo bindBukkitServerInfo(ServerServerInfo serverServerInfo);

    @Binds
    DBSystem bindBukkitDatabaseSystem(BukkitDBSystem dbSystem);

    @Binds
    ConfigSystem bindBukkitConfigSystem(BukkitConfigSystem bukkitConfigSystem);

    @Binds
    TaskSystem bindBukkitTaskSystem(BukkitTaskSystem bukkitTaskSystem);

    @Binds
    ListenerSystem bindBukkitListenerSystem(BukkitListenerSystem bukkitListenerSystem);

    @Binds
    ImportSystem bindImportSsytem(BukkitImportSystem bukkitImportSystem);

}