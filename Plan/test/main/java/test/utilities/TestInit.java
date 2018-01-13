package test.utilities;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.ServerVariableHolder;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.info.server.BukkitServerInfoManager;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
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
        new Locale();
    }

    /**
     * Init locale with mocked Plan.
     * <p>
     * requires getDataFolder mock.
     *
     * @param plan Mocked Plan
     */
    public static void initLocale(Plan plan) {
        new Locale().loadLocale();
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

        StaticHolder.register(Plan.class, planMock);
        StaticHolder.register(planMock);

        // Hacks to make APF find classes
        StaticHolder.register(IPlugin.class, planMock);
        StaticHolder.saveInstance(this.getClass(), Plan.class);
        StaticHolder.saveInstance(PowerMockRunner.class, Plan.class);

        Log.setDebugMode("console");

        File testFolder = getTestFolder();
        when(planMock.getDataFolder()).thenReturn(testFolder);

        //  Files
        File config = new File(getClass().getResource("/config.yml").getPath());
        when(planMock.getResource("config.yml")).thenReturn(new FileInputStream(config));
        File analysis = new File(getClass().getResource("/web/server.html").getPath());
        when(planMock.getResource("/web/server.html")).thenReturn(new FileInputStream(analysis));
        File player = new File(getClass().getResource("/web/player.html").getPath());
        when(planMock.getResource("/web/player.html")).thenReturn(new FileInputStream(player));

        File tempConfigFile = new File(planMock.getDataFolder(), "config.yml");
        Config iConfig = new Config(tempConfigFile, FileUtil.lines(planMock, "config.yml")) {
            @Override
            public void save() {
            }
        };
        when(planMock.getMainConfig()).thenReturn(iConfig);

        Server mockServer = mockServer();

        when(planMock.getServer()).thenReturn(mockServer);

        // Test log settings
        when(planMock.getLogger()).thenReturn(Logger.getGlobal());
        Settings.DEBUG.setValue(true);

        ServerVariableHolder serverVariableHolder = new ServerVariableHolder(mockServer);

        when(planMock.getVariable()).thenReturn(serverVariableHolder);
        BukkitServerInfoManager bukkitServerInfoManager = PowerMockito.mock(BukkitServerInfoManager.class);

        DataCache dataCache = new DataCache(planMock) {
            @Override
            public String getName(UUID uuid) {
                return "";
            }
        };
        when(planMock.getDataCache()).thenReturn(dataCache);

        when(bukkitServerInfoManager.getServerUUID()).thenReturn(serverUUID);
        when(planMock.getServerUuid()).thenReturn(serverUUID);
        when(planMock.getServerInfoManager()).thenReturn(bukkitServerInfoManager);
        ColorScheme cs = new ColorScheme(ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK, ChatColor.BLACK);
        when(planMock.getColorScheme()).thenReturn(cs);
        initLocale(null);

        RunnableFactory.activateTestMode();
    }

    private Server mockServer() {
        Server mockServer = PowerMockito.mock(Server.class);

        OfflinePlayer[] ops = new OfflinePlayer[]{MockUtils.mockPlayer(), MockUtils.mockPlayer2()};

        when(mockServer.getIp()).thenReturn("0.0.0.0");
        when(mockServer.getMaxPlayers()).thenReturn(20);
        when(mockServer.getName()).thenReturn("Bukkit");
        when(mockServer.getOfflinePlayers()).thenReturn(ops);
        ConsoleCommandSender sender = mockServerCmdSender();
        when(mockServer.getConsoleSender()).thenReturn(sender);
        return mockServer;
    }

    private ConsoleCommandSender mockServerCmdSender() {
        return new ConsoleCommandSender() {
            @Override
            public void sendMessage(String s) {
                System.out.println("Log: " + s);
            }

            @Override
            public void sendMessage(String[] strings) {
                for (String string : strings) {
                    sendMessage(string);
                }
            }

            @Override
            public Server getServer() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public Spigot spigot() {
                return null;
            }

            @Override
            public boolean isConversing() {
                return false;
            }

            @Override
            public void acceptConversationInput(String s) {

            }

            @Override
            public boolean beginConversation(Conversation conversation) {
                return false;
            }

            @Override
            public void abandonConversation(Conversation conversation) {

            }

            @Override
            public void abandonConversation(Conversation conversation, ConversationAbandonedEvent conversationAbandonedEvent) {

            }

            @Override
            public void sendRawMessage(String s) {

            }

            @Override
            public boolean isPermissionSet(String s) {
                return false;
            }

            @Override
            public boolean isPermissionSet(Permission permission) {
                return false;
            }

            @Override
            public boolean hasPermission(String s) {
                return false;
            }

            @Override
            public boolean hasPermission(Permission permission) {
                return false;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
                return null;
            }

            @Override
            public PermissionAttachment addAttachment(Plugin plugin, int i) {
                return null;
            }

            @Override
            public void removeAttachment(PermissionAttachment permissionAttachment) {

            }

            @Override
            public void recalculatePermissions() {

            }

            @Override
            public Set<PermissionAttachmentInfo> getEffectivePermissions() {
                return null;
            }

            @Override
            public boolean isOp() {
                return false;
            }

            @Override
            public void setOp(boolean b) {

            }
        };
    }

    private YamlConfiguration mockConfig() throws IOException, InvalidConfigurationException {
        File configFile = new File(getClass().getResource("/config.yml").getPath());
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.load(configFile.getAbsolutePath());
        return configuration;
    }

    public Plan getPlanMock() {
        return planMock;
    }
}
