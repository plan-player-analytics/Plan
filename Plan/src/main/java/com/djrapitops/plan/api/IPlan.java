/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.api;

import com.djrapitops.plan.ServerVariableHolder;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.systems.queue.ProcessingQueue;
import com.djrapitops.plan.systems.webserver.WebServer;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.settings.ColorScheme;

import java.io.File;
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

    Config getMainConfig();

    ColorScheme getColorScheme();

    Systems getSystems();
}