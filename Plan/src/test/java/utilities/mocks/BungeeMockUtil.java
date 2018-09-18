/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.logging.console.TestPluginLogger;
import com.djrapitops.plugin.logging.error.ConsoleErrorLogger;
import com.djrapitops.plugin.task.thread.ThreadRunnable;
import com.djrapitops.plugin.task.thread.ThreadRunnableFactory;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.TestConstants;
import utilities.mocks.objects.FakeBungeeConsole;
import utilities.mocks.objects.TestLogger;

import java.io.File;
import java.util.HashSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Bungee version of Plan (PlanBungee).
 *
 * @author Rsl1122
 */
public class BungeeMockUtil extends MockUtil {

    private PlanBungee planMock;

    private BungeeMockUtil() {
    }

    public static BungeeMockUtil setUp() {
        return new BungeeMockUtil().mockPlugin();
    }

    private BungeeMockUtil mockPlugin() {
        planMock = Mockito.mock(PlanBungee.class);
        super.planMock = planMock;
        StaticHolder.register(PlanBungee.class, planMock);
        StaticHolder.register(planMock);

        StaticHolder.saveInstance(MockitoJUnitRunner.class, PlanBungee.class);
        StaticHolder.saveInstance(ThreadRunnable.class, PlanBungee.class);

        when(planMock.getVersion()).thenCallRealMethod();
        when(planMock.getColorScheme()).thenCallRealMethod();

        ThreadRunnableFactory runnableFactory = new ThreadRunnableFactory();
        doReturn(runnableFactory).when(planMock).getRunnableFactory();

        return this;
    }

    public BungeeMockUtil withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    public BungeeMockUtil withResourceFetchingFromJar() throws Exception {
        withPluginFiles();
        return this;
    }

    public BungeeMockUtil withLogging() {
        TestLogger testLogger = new TestLogger();
        doReturn(testLogger).when(planMock).getLogger();
        TestPluginLogger testPluginLogger = new TestPluginLogger();
        doReturn(testPluginLogger).when(planMock).getPluginLogger();
        ConsoleErrorLogger consoleErrorLogger = new ConsoleErrorLogger(testPluginLogger);
        doReturn(consoleErrorLogger).when(planMock).getErrorHandler();
        return this;
    }

    public BungeeMockUtil withProxy() {
        ProxyServer proxyMock = Mockito.mock(ProxyServer.class);
        doReturn("1.12.2").when(proxyMock).getVersion();

        CommandSender console = new FakeBungeeConsole();
        doReturn(console).when(proxyMock).getConsole();

        ProxyConfig proxyConfig = Mockito.mock(ProxyConfig.class);
        doReturn(TestConstants.BUNGEE_MAX_PLAYERS).when(proxyConfig).getPlayerLimit();
        doReturn(proxyConfig).when(proxyMock).getConfig();

        PluginManager pm = Mockito.mock(PluginManager.class);
        doReturn(pm).when(proxyMock).getPluginManager();

        doReturn(proxyMock).when(planMock).getProxy();
        return this;
    }

    public BungeeMockUtil withPluginDescription() {
        File pluginYml = getFile("/bungee.yml");
        HashSet<String> empty = new HashSet<>();
        PluginDescription pluginDescription = new PluginDescription("Plan", "", "9.9.9", "Rsl1122", empty, empty, pluginYml, "");
        when(planMock.getDescription()).thenReturn(pluginDescription);
        return this;
    }

    public PlanBungee getPlanMock() {
        return planMock;
    }
}
