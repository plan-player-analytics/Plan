/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.processing.ProcessingQueue;
import com.djrapitops.plan.system.processing.processors.Processor;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plugin.IPlugin;
import com.djrapitops.plugin.api.Check;
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
public interface PlanPlugin extends IPlugin {
    @Deprecated
    Database getDB();

    @Deprecated
    ServerVariableHolder getVariable();

    @Deprecated
    UUID getServerUuid();

    @Deprecated
    InformationManager getInfoManager();

    @Deprecated
    WebServer getWebServer();

    File getDataFolder();

    @Deprecated
    ProcessingQueue getProcessingQueue();

    @Deprecated
    void addToProcessQueue(Processor... processors);

    InputStream getResource(String resource);

    @Deprecated
    Config getMainConfig();

    ColorScheme getColorScheme();

    @Deprecated
    Systems getSystems();

    boolean isReloading();

    static PlanPlugin getInstance() {
        boolean bukkitAvailable = Check.isBukkitAvailable();
        boolean bungeeAvailable = Check.isBungeeAvailable();
        if (bukkitAvailable && bungeeAvailable) {
            // TODO Test Plugin
        } else if (bungeeAvailable) {
            return Plan.getInstance();
        } else if (bukkitAvailable) {
            return PlanBungee.getInstance();
        }
        throw new IllegalAccessError("Plugin instance not available");
    }
}