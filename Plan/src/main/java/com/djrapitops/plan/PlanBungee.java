/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.command.commands.ReloadCommand;
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
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import net.md_5.bungee.api.ChatColor;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Bungee Main class.
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin<PlanBungee> implements IPlan {

    private WebServer webServer;
    private Database db;
    private BungeeServerInfoManager serverInfoManager;
    private BungeeInformationManager infoManager;
    private ServerVariableHolder variableHolder;

    private ProcessingQueue processingQueue;

    @Override
    public void onEnable() {
        try {
            super.setInstance(this);
            super.copyDefaultConfig("Plan Config | More info at https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/documentation/Configuration.md");
            super.setDebugMode(Settings.DEBUG.toString());
            super.getPluginLogger().setFolder(getDataFolder());
            super.setColorScheme(new ColorScheme(ChatColor.GREEN, ChatColor.GRAY, ChatColor.WHITE));
            super.setLogPrefix("[Plan]");
            super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
            super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

            super.onEnableDefaultTasks();

            variableHolder = new ServerVariableHolder(getProxy());

            new Locale(this).loadLocale();

            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();

            registerCommand(new ReloadCommand(this));

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

            if (!webServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized.");
                disablePlugin();
                return;
            }

            getRunnableFactory().createNew("Enable Bukkit Connection Task", new AbsRunnable() {
                @Override
                public void run() {
                    infoManager.attemptConnection();
                    infoManager.sendConfigSettings();
                }
            }).runTaskAsynchronously();

//            getProxy().registerChannel("Plan");
//            registerListener(new BungeePluginChannelListener(this));

            processingQueue = new ProcessingQueue();

            registerListener(new BungeePlayerListener(this));

            Benchmark.stop("Enable", "WebServer Initialization");
            Log.info(Locale.get(Msg.ENABLED).toString());
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.logStackTrace(e);
            disablePlugin();
        }
    }

    public static PlanBungee getInstance() {
        return getInstance(PlanBungee.class);
    }

    @Override
    public void onDisable() {
        if (processingQueue != null) {
            List<Processor> processors = processingQueue.stopAndReturnLeftovers();
            Log.info("Processing unprocessed processors. (" + processors.size() + ")");
            for (Processor processor : processors) {
                processor.process();
            }
        }
        Log.info(Locale.get(Msg.DISABLED).toString());
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
    public void restart() {
        onDisable();
        onEnable();
    }
}