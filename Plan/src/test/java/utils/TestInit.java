package test.java.utils;

import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.utilities.BenchUtil;
import com.djrapitops.plugin.utilities.log.BukkitLog;
import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.status.ProcessStatus;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
public class TestInit {

    private Plan planMock;

    /**
     *
     */
    public TestInit() {
    }

    public static TestInit init() throws Exception {
        TestInit t = new TestInit();
        t.setUp(true);
        return t;
    }

    public static TestInit init(boolean clearOnStart) throws Exception {
        TestInit t = new TestInit();
        t.setUp(clearOnStart);
        return t;
    }

    private static File getTestFolder() {
        File testFolder = new File("temporaryTestFolder");
        testFolder.mkdir();
        return testFolder;
    }

    public static void clean() throws IOException {
        clean(getTestFolder());
    }

    public static void clean(File testFolder) throws IOException {
        if (testFolder.exists() && testFolder.isDirectory()) {
            for (File f : testFolder.listFiles()) {
                Files.deleteIfExists(f.toPath());
            }
        }
    }

    @Deprecated // Use Test.init instead.
    public void setUp(boolean clearOnStart) throws Exception {
        planMock = PowerMockito.mock(Plan.class);
        StaticHolder.setInstance(Plan.class, planMock);
        StaticHolder.setInstance(planMock.getClass(), planMock);

        YamlConfiguration config = mockConfig();
        when(planMock.getConfig()).thenReturn(config);

        File testFolder = getTestFolder();
        if (clearOnStart) {
            clean(testFolder);
        }
        when(planMock.getDataFolder()).thenReturn(testFolder);

        // Html Files
        File analysis = new File(getClass().getResource("/analysis.html").getPath());
        when(planMock.getResource("analysis.html")).thenReturn(new FileInputStream(analysis));
        File player = new File(getClass().getResource("/player.html").getPath());
        when(planMock.getResource("player.html")).thenReturn(new FileInputStream(player));

        Server mockServer = mockServer();

        when(planMock.getServer()).thenReturn(mockServer);

        // Test log settings
        when(planMock.getLogger()).thenReturn(Logger.getGlobal());
        Settings.DEBUG.setValue(true);

        // Abstract Plugin Framework Mocks.
        BukkitLog<Plan> log = new BukkitLog<>(planMock, "console", "");
        BenchUtil bench = new BenchUtil(planMock);
        ServerVariableHolder serverVariableHolder = new ServerVariableHolder(mockServer);
        ProcessStatus<Plan> process = new ProcessStatus<>(planMock);
        Fetch fetch = new Fetch(planMock);

        when(planMock.getPluginLogger()).thenReturn(log);
        when(planMock.benchmark()).thenReturn(bench);
        when(planMock.getVariable()).thenReturn(serverVariableHolder);
        when(planMock.processStatus()).thenReturn(process);
        when(planMock.fetch()).thenReturn(fetch);
    }

    private Server mockServer() {
        Server mockServer = PowerMockito.mock(Server.class);
        when(mockServer.getIp()).thenReturn("0.0.0.0");
        when(mockServer.getMaxPlayers()).thenReturn(20);
        when(mockServer.getName()).thenReturn("Bukkit");
        OfflinePlayer[] ops = new OfflinePlayer[]{MockUtils.mockPlayer(), MockUtils.mockPlayer2()};
        when(mockServer.getOfflinePlayers()).thenReturn(ops);
        return mockServer;
    }

    private YamlConfiguration mockConfig() throws IOException, InvalidConfigurationException {
        File configFile = new File(getClass().getResource("/config.yml").getPath());
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(configFile.getAbsolutePath());
        return configuration;
    }

    /**
     * @return
     */
    public Plan getPlanMock() {
        return planMock;
    }
}
