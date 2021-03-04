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
import com.djrapitops.plugin.benchmarking.Timings;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.debug.CombineDebugLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.logging.debug.MemoryDebugLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Nukkit version of Plan.
 *
 * @author AuroraLS3
 */
public class PlanNukkitMocker extends Mocker {

    private PlanNukkit planMock;

    private PlanNukkitMocker() {
    }

    public static PlanNukkitMocker setUp() {
        return new PlanNukkitMocker().mockPlugin();
    }

    private PlanNukkitMocker mockPlugin() {
        planMock = Mockito.mock(PlanNukkit.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();
        doReturn("1.0.0").when(planMock).getVersion();

        cn.nukkit.plugin.PluginLogger mockedLogger = Mockito.mock(cn.nukkit.plugin.PluginLogger.class);
        RunnableFactory runnableFactory = new TestRunnableFactory();
        PluginLogger testPluginLogger = new TestPluginLogger();
        DebugLogger debugLogger = new CombineDebugLogger(new MemoryDebugLogger());
        ErrorHandler consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        Timings timings = new Timings(debugLogger);

        doReturn(mockedLogger).when(planMock).getLogger();
        doReturn(runnableFactory).when(planMock).getRunnableFactory();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        doReturn(debugLogger).when(planMock).getDebugLogger();
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        doReturn(timings).when(planMock).getTimings();

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

    PlanNukkitMocker withResourceFetchingFromJar() throws IOException {
        withPluginFiles();
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
