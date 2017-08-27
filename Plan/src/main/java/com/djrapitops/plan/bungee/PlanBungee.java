/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.bungee;

import com.djrapitops.plugin.BungeePlugin;
import com.djrapitops.plugin.settings.ColorScheme;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfoManager;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
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

        super.setColorScheme(new ColorScheme(ChatColor.GREEN, ChatColor.GRAY, ChatColor.WHITE));
        super.setLogPrefix("[Plan]");
        super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
        super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

        super.copyDefaultConfig("Plan Config | More info at https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/documentation/Configuration.md");

        super.onEnableDefaultTasks();

        variableHolder = new ServerVariableHolder(getProxy());

        Benchmark.start("WebServer Initialization");
        webServer = new WebServer(this);
        webServer.initServer();

        if (!webServer.isEnabled()) {
            Log.error("WebServer was not successfully initialized.");
        }

        serverInfoManager = new ServerInfoManager(this);
        infoManager = new InformationManager(this);
        webServer.setInfoManager(infoManager);

        Benchmark.stop("Enable", "WebServer Initialization");

        processingQueue = new ProcessingQueue();
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