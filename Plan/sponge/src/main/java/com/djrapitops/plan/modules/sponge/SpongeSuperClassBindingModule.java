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
package com.djrapitops.plan.modules.sponge;

import com.djrapitops.plan.SpongeServerShutdownSave;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.importing.EmptyImportSystem;
import com.djrapitops.plan.gathering.importing.ImportSystem;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.gathering.listeners.SpongeListenerSystem;
import com.djrapitops.plan.system.TaskSystem;
import com.djrapitops.plan.system.identification.ServerInfo;
import com.djrapitops.plan.system.identification.ServerServerInfo;
import com.djrapitops.plan.system.settings.ConfigSystem;
import com.djrapitops.plan.system.settings.SpongeConfigSystem;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.SpongeDBSystem;
import com.djrapitops.plan.system.storage.file.PlanFiles;
import com.djrapitops.plan.system.storage.file.SpongePlanFiles;
import com.djrapitops.plan.system.tasks.SpongeTaskSystem;
import dagger.Binds;
import dagger.Module;

/**
 * Module for binding Sponge specific classes to the interface implementations.
 *
 * @author Rsl1122
 */
@Module
public interface SpongeSuperClassBindingModule {

    @Binds
    PlanFiles bindSpongePlanFiles(SpongePlanFiles files);

    @Binds
    ServerInfo bindSpongeServerInfo(ServerServerInfo serverServerInfo);

    @Binds
    DBSystem bindSpongeDatabaseSystem(SpongeDBSystem dbSystem);

    @Binds
    ConfigSystem bindSpongeConfigSystem(SpongeConfigSystem spongeConfigSystem);

    @Binds
    TaskSystem bindSpongeTaskSystem(SpongeTaskSystem spongeTaskSystem);

    @Binds
    ListenerSystem bindSpongeListenerSystem(SpongeListenerSystem spongeListenerSystem);

    @Binds
    ImportSystem bindImportSystem(EmptyImportSystem emptyImportSystem);

    @Binds
    ServerShutdownSave bindSpongeServerShutdownSave(SpongeServerShutdownSave spongeServerShutdownSave);

}