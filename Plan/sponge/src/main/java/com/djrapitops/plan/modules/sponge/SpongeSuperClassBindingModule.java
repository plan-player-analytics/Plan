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
import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.SpongeSensor;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.gathering.listeners.SpongeListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.SpongeConfigSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.SpongeDBSystem;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.SpongePlanFiles;
import com.djrapitops.plan.version.SpongeVersionChecker;
import com.djrapitops.plan.version.VersionChecker;
import dagger.Binds;
import dagger.Module;
import org.spongepowered.api.world.World;

/**
 * Module for binding Sponge specific classes as interface implementations.
 *
 * @author AuroraLS3
 */
@Module
public interface SpongeSuperClassBindingModule {

    @Binds
    PlanFiles bindPlanFiles(SpongePlanFiles files);

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverInfo);

    @Binds
    DBSystem bindDBSystem(SpongeDBSystem dbSystem);

    @Binds
    ConfigSystem bindConfigSystem(SpongeConfigSystem configSystem);

    @Binds
    ListenerSystem bindListenerSystem(SpongeListenerSystem listenerSystem);

    @Binds
    ServerShutdownSave bindServerShutdownSave(SpongeServerShutdownSave shutdownSave);

    @Binds
    ServerSensor<World> bindServerSensor(SpongeSensor sensor);

    @Binds
    ServerSensor<?> bindGenericsServerSensor(ServerSensor<World> sensor);

    @Binds
    VersionChecker bindVersionChecker(SpongeVersionChecker versionChecker);
}