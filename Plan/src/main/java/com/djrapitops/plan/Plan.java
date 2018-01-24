/*
 *    Player Analytics Bukkit plugin for monitoring server activity.
 *    Copyright (C) 2017  Risto Lahtela / Rsl1122
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the Plan License. (licence.yml)
 *    Modified software can only be redistributed if allowed in the licence.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    License for more details.
 *
 *    You should have received a copy of the License
 *    along with this program.
 *    If not it should be visible on the distribution page.
 *    Or here
 *    https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/licence.yml
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.BukkitSystem;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.info.server.BukkitServerInfo;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.info.server.ServerProperties;
import com.djrapitops.plan.system.processing.processors.importing.importers.OfflinePlayerImporter;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.tasks.TaskSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.systems.info.BukkitInformationManager;
import com.djrapitops.plan.systems.info.ImporterManager;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.metrics.BStats;
import com.djrapitops.plugin.BukkitPlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.systems.TaskCenter;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * Main class for Bukkit that manages the plugin.
 * <p>
 * Everything can be accessed through this class. Use Plan.getInstance() to get
 * the initialised instance of Plan.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class Plan extends BukkitPlugin implements PlanPlugin {

    private BukkitSystem system;

    private HookHandler hookHandler; // Manages 3rd party data sources

    private BukkitInformationManager infoManager;
    private BukkitServerInfo serverInfoManager;

    private ServerProperties serverProperties;

    /**
     * Used to get the plugin-instance singleton.
     *
     * @return this object.
     */
    public static Plan getInstance() {
        return (Plan) StaticHolder.getInstance(Plan.class);
    }

    public static UUID getServerUUID() {
        return getInstance().getServerUuid();
    }

    public UUID getServerUuid() {
        return ServerInfo.getServerUUID();
    }

    /**
     * OnEnable method.
     * <p>
     * - Enables the plugin's subsystems.
     */
    @Override
    public void onEnable() {
        super.onEnable();
        try {
            FileSystem.getInstance().enable();
            ConfigSystem.getInstance().enable();

            Log.setDebugMode(Settings.DEBUG.toString());

            VersionCheckSystem.getInstance().enable();

            Benchmark.start("Enable");

            try {
                GeolocationCache.checkDB();
            } catch (UnknownHostException e) {
                Log.error("Plan Requires internet access on first run to download GeoLite2 Geolocation database.");
            } catch (IOException e) {
                throw new EnableException("Something went wrong saving the downloaded GeoLite2 Geolocation database", e);
            }

            new Locale().loadLocale();

            Theme.getInstance().enable();

            Benchmark.start("Reading server variables");
            serverProperties = new ServerProperties(getServer());
            Benchmark.stop("Enable", "Reading server variables");

            DBSystem.getInstance().enable();

            Benchmark.start("WebServer Initialization");

            serverInfoManager = new BukkitServerInfo(this);
            infoManager = new BukkitInformationManager(this);
            WebServerSystem.getInstance().enable();
            if (!WebServerSystem.isWebServerEnabled()) {
                if (Settings.WEBSERVER_DISABLED.isTrue()) {
                    Log.warn("WebServer was not initialized. (WebServer.DisableWebServer: true)");
                } else {
                    Log.error("WebServer was not initialized successfully. Is the port (" + Settings.WEBSERVER_PORT.getNumber() + ") in use?");

                }
            }
            serverInfoManager.updateServerInfo();
            infoManager.updateConnection();

            Benchmark.stop("Enable", "WebServer Initialization");

            TaskSystem.getInstance().enable();

            boolean usingBungeeWebServer = infoManager.isUsingAnotherWebServer();
            boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();

            if (!usingAlternativeIP && serverProperties.getIp().isEmpty()) {
                Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_EMPTY_IP).toString());
            }
            if (usingBungeeWebServer && usingAlternativeIP) {
                Log.info("Make sure that the alternative IP points to the Bukkit Server: " + Settings.ALTERNATIVE_IP.toString());
            }

            registerCommand("plan", new PlanCommand(this));

            Benchmark.start("Hook to 3rd party plugins");
            hookHandler = new HookHandler(this);
            Benchmark.stop("Enable", "Hook to 3rd party plugins");

            ImporterManager.registerImporter(new OfflinePlayerImporter());

            BStats bStats = new BStats(this);
            bStats.registerMetrics();

            Log.debug("Verbose debug messages are enabled.");
            Benchmark.stop("Enable", "Enable");
            Log.logDebug("Enable");
            Log.info(Locale.get(Msg.ENABLED).toString());
            StaticHolder.saveInstance(ShutdownHook.class, this.getClass());
            new ShutdownHook(this);
            if (Settings.ANALYSIS_EXPORT.isTrue()) {
                RunnableFactory.createNew(new HtmlExport(this)).runTaskAsynchronously();
            }
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.toLog(this.getClass().getName(), e);
            onDisable();
        }
    }

    @Override
    public ColorScheme getColorScheme() {
        return PlanColorScheme.create();
    }

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        system.disable();

        Log.info(Locale.get(Msg.DISABLED).toString());
        Benchmark.pluginDisabled(Plan.class);
        DebugLog.pluginDisabled(Plan.class);
        TaskCenter.cancelAllKnownTasks(Plan.class);
    }

    @Override
    public String getVersion() {
        return getDescription().getVersion();
    }

    @Override
    public void onReload() {

    }

    /**
     * Used to access active Database.
     *
     * @return the Current Database
     */
    @Deprecated
    public Database getDB() {
        return DBSystem.getInstance().getActiveDatabase();
    }

    /**
     * Used to access WebServer.
     *
     * @return the WebServer
     */
    public WebServer getWebServer() {
        return WebServerSystem.getInstance().getWebServer();
    }

    /**
     * Used to access HookHandler.
     *
     * @return HookHandler that manages Hooks to other plugins.
     */
    public HookHandler getHookHandler() {
        return hookHandler;
    }

    /**
     * Used to get the object storing server variables that are constant after
     * boot.
     *
     * @return ServerProperties
     * @see ServerProperties
     */
    @Deprecated
    public ServerProperties getVariable() {
        return serverProperties;
    }

    /**
     * Used to get the object storing server info
     *
     * @return BukkitServerInfo
     * @see BukkitServerInfo
     */
    @Deprecated
    public BukkitServerInfo getServerInfoManager() {
        return serverInfoManager;
    }

    @Deprecated
    public InformationManager getInfoManager() {
        return infoManager;
    }

    public boolean isReloading() {
        return reloading;
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void reloadConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use onReload() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public FileConfiguration getConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveConfig() {
        throw new IllegalStateException("This method should be used on this plugin. Use getMainConfig().save() instead");
    }

    /**
     * @deprecated Deprecated due to use of APF Config
     */
    @Override
    @Deprecated
    public void saveDefaultConfig() {
        throw new IllegalStateException("This method should be used on this plugin.");
    }

    public BukkitSystem getSystem() {
        return system;
    }
}
