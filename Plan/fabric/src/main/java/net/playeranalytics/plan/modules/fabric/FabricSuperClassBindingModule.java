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
package net.playeranalytics.plan.modules.fabric;

import com.djrapitops.plan.gathering.ServerSensor;
import com.djrapitops.plan.gathering.ServerShutdownSave;
import com.djrapitops.plan.gathering.listeners.ListenerSystem;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerServerInfo;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.FabricConfigSystem;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.version.VersionChecker;
import dagger.Binds;
import dagger.Module;
import net.minecraft.server.world.ServerWorld;
import net.playeranalytics.plan.FabricServerShutdownSave;
import net.playeranalytics.plan.gathering.FabricSensor;
import net.playeranalytics.plan.gathering.listeners.FabricListenerSystem;
import net.playeranalytics.plan.storage.database.FabricDBSystem;
import net.playeranalytics.plan.version.FabricVersionChecker;

/**
 * Module for binding Fabric-specific classes as interface implementations.
 *
 * @author Kopo942
 */
@Module
public interface FabricSuperClassBindingModule {

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverInfo);

    @Binds
    DBSystem bindDBSystem(FabricDBSystem dbSystem);

    @Binds
    ConfigSystem bindConfigSystem(FabricConfigSystem configSystem);

    @Binds
    ListenerSystem bindListenerSystem(FabricListenerSystem listenerSystem);

    @Binds
    ServerShutdownSave bindServerShutdownSave(FabricServerShutdownSave shutdownSave);

    @Binds
    ServerSensor<ServerWorld> bindServerSensor(FabricSensor sensor);

    @Binds
    ServerSensor<?> bindGenericsServerSensor(ServerSensor<ServerWorld> sensor);

    @Binds
    VersionChecker bindVersionChecker(FabricVersionChecker versionChecker);
}
