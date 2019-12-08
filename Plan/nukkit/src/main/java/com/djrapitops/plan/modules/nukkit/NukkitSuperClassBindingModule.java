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
package com.djrapitops.plan.modules.nukkit;

import com.djrapitops.plan.NukkitServerShutdownSave;
import com.djrapitops.plan.NukkitTaskSystem;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.importing.EmptyImportSystem;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.gathering.listeners.NukkitListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.NukkitConfigSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.NukkitDBSystem;
import dagger.Binds;
import dagger.Module;

/**
 * Module for binding Nukkit specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public interface NukkitSuperClassBindingModule {

    @Binds
    ServerInfo bindNukkitServerInfo(ServerServerInfo serverServerInfo);

    @Binds
    DBSystem bindNukkitDatabaseSystem(NukkitDBSystem dbSystem);

    @Binds
    ConfigSystem bindNukkitConfigSystem(NukkitConfigSystem nukkitConfigSystem);

    @Binds
    TaskSystem bindNukkitTaskSystem(NukkitTaskSystem nukkitTaskSystem);

    @Binds
    ListenerSystem bindNukkitListenerSystem(NukkitListenerSystem nukkitListenerSystem);

    @Binds
    ImportSystem bindImportSystem(EmptyImportSystem emptyImportSystem);

    @Binds
    ServerShutdownSave bindNukkitServerShutdownSave(NukkitServerShutdownSave nukkitServerShutdownSave);

}