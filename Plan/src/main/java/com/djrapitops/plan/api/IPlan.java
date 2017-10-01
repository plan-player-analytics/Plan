/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api;

import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.config.IConfig;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Abstraction interface for both Plan and PlanBungee.
 *
 * @author Rsl1122
 */
public interface IPlan extends IPlugin {
    Database getDB();

    ServerVariableHolder getVariable();

    UUID getServerUuid();

    InformationManager getInfoManager();

    WebServer getWebServer();

    File getDataFolder();

    ProcessingQueue getProcessingQueue();

    void addToProcessQueue(Processor... processors);

    InputStream getResource(String resource);

    IConfig getIConfig() throws IOException;

    void restart();
}