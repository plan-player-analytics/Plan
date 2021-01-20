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

import com.djrapitops.plan.Plan;
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
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.scheduler.BukkitScheduler;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.objects.TestLogger;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Bukkit version of Plan.
 *
 * @author Rsl1122
 */
public class PlanBukkitMocker extends Mocker {

    private Plan planMock;

    private PlanBukkitMocker() {
    }

    public static PlanBukkitMocker setUp() {
        return new PlanBukkitMocker().mockPlugin();
    }

    private PlanBukkitMocker mockPlugin() {
        planMock = Mockito.mock(Plan.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();
        doReturn("1.0.0").when(planMock).getVersion();

        TestLogger testLogger = new TestLogger();
        RunnableFactory runnableFactory = new TestRunnableFactory();
        PluginLogger testPluginLogger = new TestPluginLogger();
        DebugLogger debugLogger = new CombineDebugLogger(new MemoryDebugLogger());
        ErrorHandler consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        Timings timings = new Timings(debugLogger);

        doReturn(testLogger).when(planMock).getLogger();
        doReturn(runnableFactory).when(planMock).getRunnableFactory();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        doReturn(debugLogger).when(planMock).getDebugLogger();
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        doReturn(timings).when(planMock).getTimings();

        return this;
    }

    PlanBukkitMocker withDataFolder(File tempFolder) {
        doReturn(tempFolder).when(planMock).getDataFolder();
        return this;
    }

    PlanBukkitMocker withPluginDescription() {
        try (InputStream in = Files.newInputStream(getFile("/plugin.yml").toPath())) {
            PluginDescriptionFile description = new PluginDescriptionFile(in);
            doReturn(description).when(planMock).getDescription();
        } catch (IOException | InvalidDescriptionException e) {
            System.out.println("Error while setting plugin description");
        }
        return this;
    }

    PlanBukkitMocker withResourceFetchingFromJar() {
        withPluginFiles();
        return this;
    }

    PlanBukkitMocker withServer() {
        Server serverMock = Mockito.mock(Server.class);
        doReturn("").when(serverMock).getIp();
        doReturn("Bukkit").when(serverMock).getName();
        doReturn(25565).when(serverMock).getPort();
        doReturn("1.12.2").when(serverMock).getVersion();
        doReturn("32423").when(serverMock).getBukkitVersion();
        doReturn(TestConstants.SERVER_MAX_PLAYERS).when(serverMock).getMaxPlayers();
        ConsoleCommandSender sender = Mockito.mock(ConsoleCommandSender.class);
        doReturn(sender).when(serverMock).getConsoleSender();

        BukkitScheduler bukkitScheduler = Mockito.mock(BukkitScheduler.class);
        doReturn(bukkitScheduler).when(serverMock).getScheduler();

        doReturn(serverMock).when(planMock).getServer();
        return this;
    }

    Plan getPlanMock() {
        return planMock;
    }
}
