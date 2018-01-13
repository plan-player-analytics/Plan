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

import com.djrapitops.plan.api.API;
import com.djrapitops.plan.api.exceptions.EnableException;
import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.settings.theme.PlanColorScheme;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.processing.ProcessingQueue;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.file.FileSystem;
import com.djrapitops.plan.system.processing.ProcessingQueue;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.ConfigSystem;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import com.djrapitops.plan.system.webserver.pagecache.ResponseCache;
import com.djrapitops.plan.systems.Systems;
import com.djrapitops.plan.systems.cache.DataCache;
import com.djrapitops.plan.systems.cache.GeolocationCache;
import com.djrapitops.plan.systems.file.database.DBSystem;
import com.djrapitops.plan.systems.info.BukkitInformationManager;
import com.djrapitops.plan.systems.info.ImporterManager;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plan.systems.info.server.BukkitServerInfoManager;
import com.djrapitops.plan.systems.listeners.*;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.systems.processing.importing.importers.OfflinePlayerImporter;
import com.djrapitops.plan.systems.tasks.TaskSystem;
import com.djrapitops.plan.systems.webserver.WebServer;
import com.djrapitops.plan.systems.webserver.WebServerSystem;
import com.djrapitops.plan.systems.webserver.pagecache.PageCache;
import com.djrapitops.plan.utilities.file.export.HtmlExport;
import com.djrapitops.plan.utilities.metrics.BStats;
import com.djrapitops.plugin.BukkitPlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.systems.TaskCenter;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
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

    private API api;

    private Systems systems;

    private ProcessingQueue processingQueue;
    private HookHandler hookHandler; // Manages 3rd party data sources

    private BukkitInformationManager infoManager;
    private BukkitServerInfoManager serverInfoManager;

    private ServerVariableHolder serverVariableHolder;

    /**
     * Used to get the PlanAPI. @see API
     *
     * @return API of the current instance of Plan.
     * @throws IllegalStateException If onEnable method has not been called on
     *                               Plan and the instance is null.
     * @throws NoClassDefFoundError  If Plan is not installed.
     */
    public static API getPlanAPI() throws NoClassDefFoundError {
        Plan instance = getInstance();
        if (instance == null) {
            throw new IllegalStateException("Plugin not enabled properly, Singleton instance is null.");
        }
        return instance.getApi();
    }

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
        return serverInfoManager.getServerUUID();
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
            systems = new Systems(this);
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
            serverVariableHolder = new ServerVariableHolder(getServer());
            Benchmark.stop("Enable", "Reading server variables");

            DBSystem.getInstance().enable();

            Benchmark.start("WebServer Initialization");
            processingQueue = new ProcessingQueue();

            serverInfoManager = new BukkitServerInfoManager(this);
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

            if (!reloading) {
                registerListeners();
            }
            PlanPlayerListener.setCountKicks(true);

            TaskSystem.getInstance().enable();

            this.api = new API(this);

            boolean usingBungeeWebServer = infoManager.isUsingAnotherWebServer();
            boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();

            if (!usingAlternativeIP && serverVariableHolder.getIp().isEmpty()) {
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
        //Clears the page cache
        ResponseCache.clearCache();

        // Processes unprocessed processors
        if (processingQueue != null) {
            List<Processor> processors = processingQueue.stopAndReturnLeftovers();
            if (!reloading) {
                Log.info("Processing unprocessed processors. (" + processors.size() + ")"); // TODO Move to Locale
                for (Processor processor : processors) {
                    processor.process();
                }
            } else {
                RunnableFactory.createNew("Re-Add processors", new AbsRunnable() {
                    @Override
                    public void run() {
                        addToProcessQueue(processors.toArray(new Processor[processors.size()]));
                        cancel();
                    }
                }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
            }
        }

        systems.close();

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
        ConfigSystem.reload();
    }

    private void registerListeners() {
        Benchmark.start("Register Listeners");
        registerListener(new PlanPlayerListener(this));
        registerListener(new PlanChatListener(this));
        registerListener(new PlanGamemodeChangeListener(this));
        registerListener(new PlanWorldChangeListener(this));
        registerListener(new PlanCommandPreprocessListener(this));
        registerListener(new PlanDeathEventListener(this));
        Benchmark.stop("Enable", "Register Listeners");
    }

    /**
     * Used to access Cache.
     *
     * @return Current instance of the DataCache
     */
    public DataCache getDataCache() {
        return getInfoManager().getDataCache();
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
     * @return ServerVariableHolder
     * @see ServerVariableHolder
     */
    public ServerVariableHolder getVariable() {
        return serverVariableHolder;
    }

    /**
     * Used to get the object storing server info
     *
     * @return BukkitServerInfoManager
     * @see BukkitServerInfoManager
     */
    public BukkitServerInfoManager getServerInfoManager() {
        return serverInfoManager;
    }

    public ProcessingQueue getProcessingQueue() {
        return processingQueue;
    }

    public void addToProcessQueue(Processor... processors) {
        if (!reloading) {
            for (Processor processor : processors) {
                if (processor == null) {
                    continue;
                }
                processingQueue.addToQueue(processor);
            }
        } else {
            RunnableFactory.createNew("Re-Add processors", new AbsRunnable() {
                @Override
                public void run() {
                    addToProcessQueue(processors);
                    cancel();
                }
            }).runTaskLaterAsynchronously(TimeAmount.SECOND.ticks() * 5L);
        }
    }

    @Override
    public Config getMainConfig() {
        return ConfigSystem.getInstance().getConfig();
    }

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

    /**
     * Method for getting the API.
     * <p>
     * Created due to necessity for testing, but can be used.
     * For direct API getter use {@code Plan.getPlanAPI()}.
     * <p>
     * If Plan is reloaded a new API instance is created.
     *
     * @return Plan API instance.
     */
    public API getApi() {
        return api;
    }

    public Systems getSystems() {
        return systems;
    }
}
