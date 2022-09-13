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
package utilities.mocks;

import cn.nukkit.Server;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.plugin.PluginDescription;
import cn.nukkit.scheduler.ServerScheduler;
import com.djrapitops.plan.PlanNukkit;
import com.djrapitops.plan.commands.use.ColorScheme;
import org.mockito.Mockito;
import utilities.TestConstants;

import java.io.File;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Nukkit version of Plan.
 *
 * @author AuroraLS3
 */
public class PlanNukkitMocker {

    private PlanNukkit planMock;

    private PlanNukkitMocker() {
    }

    public static PlanNukkitMocker setUp() {
        return new PlanNukkitMocker().mockPlugin();
    }

    private PlanNukkitMocker mockPlugin() {
        planMock = Mockito.mock(PlanNukkit.class);

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();

        cn.nukkit.plugin.PluginLogger mockedLogger = Mockito.mock(cn.nukkit.plugin.PluginLogger.class);

        doReturn(mockedLogger).when(planMock).getLogger();

        return this;
    }

    PlanNukkitMocker withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    PlanNukkitMocker withPluginDescription() {
        PluginDescription description = Mockito.mock(PluginDescription.class);
        doReturn("1.0.0").when(description).getVersion();
        doReturn(description).when(planMock).getDescription();
        return this;
    }

    PlanNukkitMocker withServer() {
        Server serverMock = Mockito.mock(Server.class);
        doReturn("").when(serverMock).getIp();
        doReturn("Nukkit").when(serverMock).getName();
        doReturn(25565).when(serverMock).getPort();
        doReturn("1.12.2").when(serverMock).getVersion();
        doReturn("32423").when(serverMock).getNukkitVersion();
        doReturn(TestConstants.SERVER_MAX_PLAYERS).when(serverMock).getMaxPlayers();
        ConsoleCommandSender sender = Mockito.mock(ConsoleCommandSender.class);
        doReturn(sender).when(serverMock).getConsoleSender();

        ServerScheduler scheduler = Mockito.mock(ServerScheduler.class);
        doReturn(scheduler).when(serverMock).getScheduler();

        doReturn(serverMock).when(planMock).getServer();
        return this;
    }

    PlanNukkit getPlanMock() {
        return planMock;
    }
}
