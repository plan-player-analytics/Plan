/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.bungee;

import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.settings.ColorScheme;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfoManager;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.Check;
import net.md_5.bungee.api.ChatColor;

import java.io.InputStream;
import java.util.List;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class PlanBungee extends BungeePlugin<PlanBungee> implements IPlan {

    private WebServer webServer;
    private Database db;
    private ServerInfoManager serverInfoManager;
    private InformationManager infoManager;
    private ServerVariableHolder variableHolder;

    private ProcessingQueue processingQueue;

    public PlanBungee() {
    }

    @Override
    public void onEnable() {
        super.setInstance(this);
        super.setDebugMode(Settings.DEBUG.toString());
        super.getPluginLogger().setFolder(getDataFolder());
        super.setColorScheme(new ColorScheme(ChatColor.GREEN, ChatColor.GRAY, ChatColor.WHITE));
        super.setLogPrefix("[Plan]");
        super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
        super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

        super.copyDefaultConfig("Plan Config | More info at https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/documentation/Configuration.md");

        super.onEnableDefaultTasks();

        variableHolder = new ServerVariableHolder(getProxy());

        new Locale(this).loadLocale();

        processingQueue = new ProcessingQueue();

        Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
        if (!initDatabase()) {
            disablePlugin();
            return;
        }

        String ip = variableHolder.getIp();
        if ("0.0.0.0".equals(ip)) {
            Log.error("IP setting still 0.0.0.0 - Set up AlternativeIP/IP that connects to the Proxy server.");
        }

        Benchmark.start("WebServer Initialization");
        webServer = new WebServer(this);
        webServer.initServer();

        if (!webServer.isEnabled()) {
            Log.error("WebServer was not successfully initialized.");
            disablePlugin();
            return;
        }

        serverInfoManager = new ServerInfoManager(this);
        infoManager = new InformationManager(this);
        webServer.setInfoManager(infoManager);

        Benchmark.stop("Enable", "WebServer Initialization");
        Log.info(Locale.get(Msg.ENABLED).toString());
    }

    public static PlanBungee getInstance() {
        return getInstance(PlanBungee.class);
    }

    @Override
    public void onDisable() {
        List<Processor> processors = processingQueue.stopAndReturnLeftovers();
        Log.info("Processing unprocessed processors. (" + processors.size() + ")");
        for (Processor processor : processors) {
            processor.process();
        }
        Log.info(Locale.get(Msg.DISABLED).toString());
    }

    private boolean initDatabase() {
        db = new MySQLDB(this);
        return Check.errorIfFalse(db.init(), Locale.get(Msg.ENABLE_DB_FAIL_DISABLE_INFO).toString());
    }

    @Override
    public Database getDB() {
        return db;
    }

    @Override
    public ServerInfoManager getServerInfoManager() {
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
}