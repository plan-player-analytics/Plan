/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package utilities.mocks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.task.ThreadRunnable;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import utilities.Teardown;
import utilities.TestConstants;
import utilities.mocks.objects.FakeBungeeConsole;
import utilities.mocks.objects.TestLogger;

import java.io.File;
import java.util.HashSet;

import static org.mockito.Mockito.doCallRealMethod;
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
        RunnableFactory.activateTestMode();
        Teardown.resetSettingsTempValues();
        return new BungeeMockUtil().mockPlugin();
    }

    private BungeeMockUtil mockPlugin() {
        planMock = Mockito.mock(PlanBungee.class);
        super.planMock = planMock;
        StaticHolder.register(PlanBungee.class, planMock);
        StaticHolder.register(planMock);

        StaticHolder.saveInstance(MockitoJUnitRunner.class, PlanBungee.class);
        StaticHolder.saveInstance(ThreadRunnable.class, PlanBungee.class);

        doCallRealMethod().when(planMock).getVersion();
        doCallRealMethod().when(planMock).getColorScheme();
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
        doCallRealMethod().when(planMock).log(Mockito.anyString(), Mockito.anyString());
        TestLogger testLogger = new TestLogger();
        when(planMock.getLogger()).thenReturn(testLogger);
        return this;
    }

    public BungeeMockUtil withProxy() {
        ProxyServer proxyMock = Mockito.mock(ProxyServer.class);
        when(proxyMock.getVersion()).thenReturn("1.12.2");

        CommandSender console = new FakeBungeeConsole();
        when(proxyMock.getConsole()).thenReturn(console);

        ProxyConfig proxyConfig = Mockito.mock(ProxyConfig.class);
        when(proxyConfig.getPlayerLimit()).thenReturn(TestConstants.BUNGEE_MAX_PLAYERS);
        when(proxyMock.getConfig()).thenReturn(proxyConfig);

        PluginManager pm = Mockito.mock(PluginManager.class);
        when(proxyMock.getPluginManager()).thenReturn(pm);

        when(planMock.getProxy()).thenReturn(proxyMock);
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