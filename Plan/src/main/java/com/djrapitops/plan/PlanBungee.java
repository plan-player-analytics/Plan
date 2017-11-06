/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.command.PlanBungeeCommand;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.BungeeInformationManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.BungeeServerInfoManager;
import main.java.com.djrapitops.plan.systems.listeners.BungeePlayerListener;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import net.md_5.bungee.api.ChatColor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin implements IPlan {

    private Config config;

    private WebServer webServer;
    private Database db;
    private BungeeServerInfoManager serverInfoManager;
    private BungeeInformationManager infoManager;
    private ServerVariableHolder variableHolder;

    private ProcessingQueue processingQueue;

    @Override
    public void onEnable() {
        super.onEnable();
        try {
            File configFile = new File(getDataFolder(), "config.yml");
            config = new Config(configFile);
            config.copyDefaults(FileUtil.lines(this, "bungeeconfig.yml"));
            config.save();

            Log.setDebugMode(Settings.DEBUG.toString());

            String currentVersion = getVersion();
            String githubVersionUrl = "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml";
            String spigotUrl = "https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            try {
                if (Version.checkVersion(currentVersion, githubVersionUrl) || Version.checkVersion(currentVersion, spigotUrl)) {
                    Log.infoColor("§a----------------------------------------");
                    Log.infoColor("§aNew version is available at https://www.spigotmc.org/resources/plan-player-analytics.32536/");
                    Log.infoColor("§a----------------------------------------");
                } else {
                    Log.info("You're using the latest version.");
                }
            } catch (IOException e) {
                Log.error("Failed to check newest version number");
            }
            variableHolder = new ServerVariableHolder(getProxy());

            new Locale(this).loadLocale();

            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();

            registerCommand("planbungee", new PlanBungeeCommand(this));

            String ip = variableHolder.getIp();
            if ("0.0.0.0".equals(ip)) {
                Log.error("IP setting still 0.0.0.0 - Configure AlternativeIP/IP that connects to the Proxy server.");
                Log.info("Player Analytics partially enabled (Use /planbungee to reload config)");
                return;
            }

            Benchmark.start("WebServer Initialization");
            webServer = new WebServer(this);

            serverInfoManager = new BungeeServerInfoManager(this);
            infoManager = new BungeeInformationManager(this);
            webServer.initServer();
            serverInfoManager.loadServerInfo();


            if (!webServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized.");
                onDisable();
                return;
            }

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
        if (webServer != null) {
            webServer.stop();
        }
        if (db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        }
        Log.info(Locale.get(Msg.DISABLED).toString());
        super.onDisable();
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public void onReload() {
        try {
            config.read();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void initDatabase() throws DatabaseInitException {
        db = new MySQLDB(this);
        db.init();
    }

    @Override
    public Database getDB() {
        return db;
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
        return webServer;
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
        return config;
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

}