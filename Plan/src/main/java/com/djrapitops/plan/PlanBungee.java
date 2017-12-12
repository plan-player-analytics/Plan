/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Priority;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.systems.NotificationCenter;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.command.PlanBungeeCommand;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.systems.Systems;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.BungeeServerInfoManager;
import main.java.com.djrapitops.plan.systems.listeners.BungeePlayerListener;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.store.FileSystem;
import main.java.com.djrapitops.plan.systems.store.config.ConfigSystem;
import main.java.com.djrapitops.plan.systems.store.database.DBSystem;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.systems.webserver.WebServerSystem;
import main.java.com.djrapitops.plan.utilities.file.export.HtmlExport;
import net.md_5.bungee.api.ChatColor;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements IPlan {

    private Theme theme;

    private Systems systems;

    private BungeeServerInfoManager serverInfoManager;
    private BungeeInformationManager infoManager;
    private ServerVariableHolder variableHolder;

    private ProcessingQueue processingQueue;

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            systems = new Systems(this);
            FileSystem.getInstance().init();
            ConfigSystem.getInstance().init();

            Log.setDebugMode(Settings.DEBUG.toString());

            String currentVersion = getVersion();
            String githubVersionUrl = "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml";
            String spigotUrl = "https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            try {
                if (Version.checkVersion(currentVersion, githubVersionUrl) || Version.checkVersion(currentVersion, spigotUrl)) {
                    Log.infoColor("§a----------------------------------------");
                    Log.infoColor("§aNew version is available at https://www.spigotmc.org/resources/plan-player-analytics.32536/");
                    Log.infoColor("§a----------------------------------------");
                    NotificationCenter.addNotification(Priority.HIGH, "New Version is available at https://www.spigotmc.org/resources/plan-player-analytics.32536/");
                } else {
                    Log.info("You're using the latest version.");
                }
            } catch (IOException e) {
                Log.error("Failed to check newest version number");
            }
            variableHolder = new ServerVariableHolder(getProxy());

            new Locale().loadLocale();

            theme = new Theme();

            DBSystem.getInstance().init();

            registerCommand("planbungee", new PlanBungeeCommand(this));

            String ip = variableHolder.getIp();
            if ("0.0.0.0".equals(ip)) {
                Log.error("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
                Log.info("Player Analytics partially enabled (Use /planbungee to reload config)");
                return;
            }

            Benchmark.start("WebServer Initialization");

            serverInfoManager = new BungeeServerInfoManager(this);
            infoManager = new BungeeInformationManager(this);

            WebServerSystem.getInstance().init();
            serverInfoManager.loadServerInfo();

            RunnableFactory.createNew("Enable Bukkit Connection Task", new AbsRunnable() {
                @Override
                public void run() {
                    infoManager.attemptConnection();
                    infoManager.sendConfigSettings();
                }
            }).runTaskAsynchronously();
            RunnableFactory.createNew("Player Count task", new TPSCountTimer(this))
                    .runTaskTimerAsynchronously(1000, TimeAmount.SECOND.ticks());
            RunnableFactory.createNew("NetworkPageContentUpdateTask", new AbsRunnable("NetworkPageContentUpdateTask") {
                @Override
                public void run() {
                    infoManager.updateNetworkPageContent();
                }
            }).runTaskTimerAsynchronously(1500, TimeAmount.MINUTE.ticks());

            processingQueue = new ProcessingQueue();

            registerListener(new BungeePlayerListener(this));

            Log.logDebug("Enable", "WebServer Initialization");
            Log.info(Locale.get(Msg.ENABLED).toString());
            if (Settings.ANALYSIS_EXPORT.isTrue()) {
                RunnableFactory.createNew(new HtmlExport(this)).runTaskAsynchronously();
            }
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.toLog(this.getClass().getName(), e);
            onDisable();
        }
    }

    public static PlanBungee getInstance() {
        return (PlanBungee) StaticHolder.getInstance(PlanBungee.class);
    }

    @Override
    public void onDisable() {
        if (processingQueue != null) {
            try {
                processingQueue.stop();
            } catch (IllegalArgumentException ignored) {
                /*ignored*/
            }
        }
        systems.close();
        Log.info(Locale.get(Msg.DISABLED).toString());
        super.onDisable();
    }

    @Override
    public String getVersion() {
        return super.getDescription().getVersion();
    }

    @Override
    public void onReload() {
        ConfigSystem.reload();
    }

    @Override
    @Deprecated
    public Database getDB() {
        return DBSystem.getInstance().getActiveDatabase();
    }

    public BungeeServerInfoManager getServerInfoManager() {
        return serverInfoManager;
    }

    @Override
    public InformationManager getInfoManager() {
        return infoManager;
    }

    @Override
    public WebServer getWebServer() {
        return WebServerSystem.getInstance().getWebServer();
    }


    @Override
    public ProcessingQueue getProcessingQueue() {
        return processingQueue;
    }

    @Override
    public void addToProcessQueue(Processor... processors) {
        for (Processor processor : processors) {
            processingQueue.addToQueue(processor);
        }
    }

    @Override
    public InputStream getResource(String resource) {
        return getResourceAsStream(resource);
    }

    @Override
    public Config getMainConfig() {
        return ConfigSystem.getInstance().getConfig();
    }

    @Override
    public ColorScheme getColorScheme() {
        try {
            ChatColor mainColor = ChatColor.getByChar(Settings.COLOR_MAIN.toString().charAt(1));
            ChatColor secColor = ChatColor.getByChar(Settings.COLOR_SEC.toString().charAt(1));
            ChatColor terColor = ChatColor.getByChar(Settings.COLOR_TER.toString().charAt(1));
            return new ColorScheme(mainColor, secColor, terColor);
        } catch (Exception e) {
            Log.infoColor(ChatColor.RED + "Customization, Chat colors set-up wrong, using defaults.");
            return new ColorScheme(ChatColor.DARK_GREEN, ChatColor.GRAY, ChatColor.WHITE);
        }
    }

    @Override
    public ServerVariableHolder getVariable() {
        return variableHolder;
    }

    public static UUID getServerUUID() {
        return getInstance().getServerUuid();
    }

    public UUID getServerUuid() {
        return serverInfoManager.getServerUUID();
    }

    @Override
    public Theme getTheme() {
        return theme;
    }

    @Override
    public Systems getSystems() {
        return systems;
    }
}