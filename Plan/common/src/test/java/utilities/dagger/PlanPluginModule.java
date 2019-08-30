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
package utilities.dagger;

import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.system.importing.EmptyImportSystem;
import com.djrapitops.plan.system.importing.ImportSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerServerInfo;
import com.djrapitops.plan.system.settings.BukkitConfigSystem;
import com.djrapitops.plan.system.settings.ConfigSystem;
import com.djrapitops.plugin.command.CommandNode;
import dagger.Binds;
import dagger.Module;

import javax.inject.Named;

/**
 * Dagger module for binding Plan instance.
 *
 * @author Rsl1122
 */
@Module
public interface PlanPluginModule {

    @Binds
    @Named("mainCommand")
    CommandNode bindMainCommand(PlanCommand command);

    @Binds
    ImportSystem bindImportSystem(EmptyImportSystem emptyImportSystem);

    @Binds
    ConfigSystem bindBukkitConfigSystem(BukkitConfigSystem bukkitConfigSystem);

    @Binds
    ServerInfo bindServerInfo(ServerServerInfo serverServerInfo);

}