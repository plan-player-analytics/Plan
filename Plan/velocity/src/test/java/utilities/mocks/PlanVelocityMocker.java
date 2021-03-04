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

import com.djrapitops.plan.PlanVelocity;
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
import com.velocitypowered.api.proxy.ProxyServer;
import org.mockito.Mockito;
import utilities.mocks.objects.TestRunnableFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Velocity version of Plan (PlanVelocity).
 *
 * @author AuroraLS3
 */
public class PlanVelocityMocker extends Mocker {

    private PlanVelocity planMock;

    private PlanVelocityMocker() {
    }

    public static PlanVelocityMocker setUp() {
        return new PlanVelocityMocker().mockPlugin();
    }

    private PlanVelocityMocker mockPlugin() {
        planMock = Mockito.mock(PlanVelocity.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();
        doReturn("1.0.0").when(planMock).getVersion();

        RunnableFactory runnableFactory = new TestRunnableFactory();
        PluginLogger testPluginLogger = new TestPluginLogger();
        DebugLogger debugLogger = new CombineDebugLogger(new MemoryDebugLogger());
        ErrorHandler consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        Timings timings = new Timings(debugLogger);

        doReturn(runnableFactory).when(planMock).getRunnableFactory();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        doReturn(debugLogger).when(planMock).getDebugLogger();
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        doReturn(timings).when(planMock).getTimings();

        return this;
    }

    public PlanVelocityMocker withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public PlanVelocityMocker withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    @Deprecated
    public PlanVelocityMocker withLogging() {
        return this;
    }

    public PlanVelocityMocker withProxy() {
        ProxyServer server = Mockito.mock(ProxyServer.class);

        InetSocketAddress ip = new InetSocketAddress(25565);

        doReturn(new ArrayList<>()).when(server).getAllServers();
        doReturn(ip).when(server).getBoundAddress();

        doReturn(server).when(planMock).getProxy();
        return this;
    }

    public PlanVelocity getPlanMock() {
        return planMock;
    }
}
