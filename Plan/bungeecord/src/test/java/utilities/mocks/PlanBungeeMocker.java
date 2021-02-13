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

import com.djrapitops.plan.PlanBungee;
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
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.objects.TestLogger;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.util.HashSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Bungee version of Plan (PlanBungee).
 *
 * @author AuroraLS3
 */
public class PlanBungeeMocker extends Mocker {

    private PlanBungee planMock;

    private PlanBungeeMocker() {
    }

    public static PlanBungeeMocker setUp() {
        return new PlanBungeeMocker().mockPlugin();
    }

    private PlanBungeeMocker mockPlugin() {
        planMock = Mockito.mock(PlanBungee.class);
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

    PlanBungeeMocker withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    PlanBungeeMocker withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    @SuppressWarnings("deprecation")
    PlanBungeeMocker withProxy() {
        ProxyServer proxyMock = Mockito.mock(ProxyServer.class);
        doReturn("1.12.2").when(proxyMock).getVersion();

        CommandSender console = Mockito.mock(CommandSender.class);
        doReturn(console).when(proxyMock).getConsole();

        ProxyConfig proxyConfig = Mockito.mock(ProxyConfig.class);
        doReturn(TestConstants.BUNGEE_MAX_PLAYERS).when(proxyConfig).getPlayerLimit();
        doReturn(proxyConfig).when(proxyMock).getConfig();

        PluginManager pm = Mockito.mock(PluginManager.class);
        doReturn(pm).when(proxyMock).getPluginManager();

        doReturn(proxyMock).when(planMock).getProxy();
        return this;
    }

    PlanBungeeMocker withPluginDescription() {
        File pluginYml = getFile("/bungee.yml");
        HashSet<String> empty = new HashSet<>();
        PluginDescription pluginDescription = new PluginDescription("Plan", "", "9.9.9", "AuroraLS3", empty, empty, pluginYml, "");
        when(planMock.getDescription()).thenReturn(pluginDescription);
        return this;
    }

    PlanBungee getPlanMock() {
        return planMock;
    }
}
