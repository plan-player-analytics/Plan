package test.java.utils;

import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.config.BukkitConfig;
import com.djrapitops.plugin.config.IConfig;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.IRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.BenchUtil;
import com.djrapitops.plugin.utilities.log.BukkitLog;
import com.djrapitops.plugin.utilities.player.Fetch;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfoManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.powermock.api.mockito.PowerMockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
public class TestInit {

    private static final UUID serverUUID = UUID.fromString("9a27457b-f1a2-4b71-be7f-daf2170a1b66");
    private Plan planMock;

    /**
     * Init locale with empty messages.
     * <p>
     * Does not load any messages from anywhere because that would cause exceptions.
     */
    public static void initEmptyLocale() {
        new Locale(null);
    }

    /**
     * Init locale with mocked Plan.
     * <p>
     * requires getDataFolder mock.
     *
     * @param plan Mocked Plan
     */
    public static void initLocale(Plan plan) {
        new Locale(plan).loadLocale();
    }

    public static TestInit init() throws Exception {
        TestInit t = new TestInit();
        t.setUp();
        return t;
    }

    static File getTestFolder() {
        File testFolder = new File("temporaryTestFolder");
        testFolder.mkdir();
        return testFolder;
    }

    public static UUID getServerUUID() {
        return serverUUID;
    }

    private void setUp() throws Exception {
        planMock = PowerMockito.mock(Plan.class);
        StaticHolder.setInstance(Plan.class, planMock);
        StaticHolder.setInstance(planMock.getClass(), planMock);

        File testFolder = getTestFolder();
        when(planMock.getDataFolder()).thenReturn(testFolder);

        YamlConfiguration config = mockConfig();
        when(planMock.getConfig()).thenReturn(config);
        IConfig iConfig = new BukkitConfig(planMock, "config.yml");
        iConfig.copyFromStream(getClass().getResource("/config.yml").openStream());
        when(planMock.getIConfig()).thenReturn(iConfig);

        // Html Files
        File analysis = new File(getClass().getResource("/server.html").getPath());
        when(planMock.getResource("server.html")).thenReturn(new FileInputStream(analysis));
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
        Fetch fetch = new Fetch(planMock);

        when(planMock.getPluginLogger()).thenReturn(log);
        when(planMock.benchmark()).thenReturn(bench);
        when(planMock.getVariable()).thenReturn(serverVariableHolder);
        when(planMock.fetch()).thenReturn(fetch);
        ServerInfoManager serverInfoManager = PowerMockito.mock(ServerInfoManager.class);

        when(serverInfoManager.getServerUUID()).thenReturn(serverUUID);
        when(planMock.getServerInfoManager()).thenReturn(serverInfoManager);
        RunnableFactory<Plan> runnableFactory = mockRunnableFactory();
        when(planMock.getRunnableFactory()).thenReturn(runnableFactory);
        ColorScheme cs = new ColorScheme(ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK);
        when(planMock.getColorScheme()).thenReturn(cs);
        initLocale(planMock);
    }

    private RunnableFactory<Plan> mockRunnableFactory() {
        return new RunnableFactory<Plan>(planMock) {
            @Override
            public IRunnable createNew(String name, final AbsRunnable runnable) {
                IRunnable iRunnable = new IRunnable() {
                    Timer timer = new Timer();
                    TimerTask task = new TimerTask() {
                        @Override
                        public void run() {
                            runnable.run();
                        }
                    };

                    @Override
                    public String getTaskName() {
                        return name;
                    }

                    @Override
                    public void cancel() {
                        timer.cancel();
                    }

                    @Override
                    public int getTaskId() {
                        return runnable.getTaskId();
                    }

                    @Override
                    public ITask runTask() {
                        task.run();
                        return null;
                    }

                    @Override
                    public ITask runTaskAsynchronously() {
                        new Thread(this::runTask).start();
                        return null;
                    }

                    @Override
                    public ITask runTaskLater(long l) {
                        timer.schedule(task, convertTicksToMillis(l));
                        return null;
                    }

                    @Override
                    public ITask runTaskLaterAsynchronously(long l) {
                        new Thread(() -> timer.schedule(task, convertTicksToMillis(l))).start();
                        return null;
                    }

                    @Override
                    public ITask runTaskTimer(long l, long l1) {
                        timer.scheduleAtFixedRate(task, convertTicksToMillis(l), convertTicksToMillis(l1));
                        return null;
                    }

                    @Override
                    public ITask runTaskTimerAsynchronously(long l, long l1) {
                        new Thread(() -> timer.scheduleAtFixedRate(task, convertTicksToMillis(l), convertTicksToMillis(l1)));
                        return null;
                    }

                    private long convertTicksToMillis(long ticks) {
                        return ticks * 50;
                    }
                };
                runnable.setCancellable(iRunnable);
                return iRunnable;
            }
        };
    }

    private Server mockServer() {
        Server mockServer = PowerMockito.mock(Server.class);

        OfflinePlayer[] ops = new OfflinePlayer[]{MockUtils.mockPlayer(), MockUtils.mockPlayer2()};

        when(mockServer.getIp()).thenReturn("0.0.0.0");
        when(mockServer.getMaxPlayers()).thenReturn(20);
        when(mockServer.getName()).thenReturn("Bukkit");
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
