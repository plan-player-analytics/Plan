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
package main.java.com.djrapitops.plan;

import com.djrapitops.plugin.BukkitPlugin;
import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.Priority;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.config.Config;
import com.djrapitops.plugin.api.systems.NotificationCenter;
import com.djrapitops.plugin.api.systems.TaskCenter;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.DebugLog;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.api.exceptions.PlanEnableException;
import main.java.com.djrapitops.plan.command.PlanCommand;
import main.java.com.djrapitops.plan.data.plugin.HookHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.settings.Settings;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;
import main.java.com.djrapitops.plan.settings.theme.Theme;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.GeolocationCache;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.info.ImporterManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.BukkitServerInfoManager;
import main.java.com.djrapitops.plan.systems.listeners.*;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.processing.importing.importers.OfflinePlayerImporter;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import main.java.com.djrapitops.plan.utilities.metrics.BStats;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
public class Plan extends BukkitPlugin implements IPlan {

    private API api;

    private Config config;
    private Theme theme;

    private ProcessingQueue processingQueue;
    private HookHandler hookHandler; // Manages 3rd party data sources

    private Database db;
    private Set<Database> databases;

    private WebServer webServer;

    private BukkitInformationManager infoManager;
    private BukkitServerInfoManager serverInfoManager;

    private ServerVariableHolder serverVariableHolder;
    private TPSCountTimer tpsCountTimer;
    private int bootAnalysisTaskID = -1;

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
            File dataFolder = getDataFolder();
            dataFolder.mkdirs();
            File configFile = new File(dataFolder, "config.yml");
            config = new Config(configFile);
            config.copyDefaults(FileUtil.lines(this, "config.yml"));
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
                    NotificationCenter.addNotification(Priority.HIGH, "New Version is available at https://www.spigotmc.org/resources/plan-player-analytics.32536/");
                } else {
                    Log.info("You're using the latest version.");
                }
            } catch (IOException e) {
                Log.error("Failed to check newest version number");
            }

            Benchmark.start("Enable");

            try {
                GeolocationCache.checkDB();
            } catch (UnknownHostException e) {
                Log.error("Plan Requires internet access on first run to download GeoLite2 Geolocation database.");
            } catch (IOException e) {
                throw new PlanEnableException("Something went wrong saving the downloaded GeoLite2 Geolocation database", e);
            }

            new Locale(this).loadLocale();

            theme = new Theme();

            Benchmark.start("Reading server variables");
            serverVariableHolder = new ServerVariableHolder(getServer());
            Benchmark.stop("Enable", "Reading server variables");

            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();
            Benchmark.stop("Enable", "Init Database");

            Benchmark.start("WebServer Initialization");
            webServer = new WebServer(this);

            processingQueue = new ProcessingQueue();

            serverInfoManager = new BukkitServerInfoManager(this);
            infoManager = new BukkitInformationManager(this);

            webServer.initServer();
            if (!webServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized. Is the port (" + Settings.WEBSERVER_PORT.getNumber() + ") in use?");
            }
            serverInfoManager.updateServerInfo();

            Benchmark.stop("Enable", "WebServer Initialization");

            if (!reloading) {
                registerListeners();
            }
            PlanPlayerListener.setCountKicks(true);
            registerTasks();

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
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.toLog(this.getClass().getName(), e);
            onDisable();
        }
    }

    private void registerTasks() {
        String bootAnalysisMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_INFO).toString();
        String bootAnalysisRunMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_RUN_INFO).toString();

        Benchmark.start("Task Registration");
        tpsCountTimer = new TPSCountTimer(this);
        RunnableFactory.createNew(tpsCountTimer).runTaskTimer(1000, TimeAmount.SECOND.ticks());

        // Analysis refresh settings
        int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;
        long analysisPeriod = analysisRefreshMinutes * TimeAmount.MINUTE.ticks();

        Log.info(bootAnalysisMsg);

        ITask bootAnalysisTask = RunnableFactory.createNew("BootAnalysisTask", new AbsRunnable() {
            @Override
            public void run() {
                Log.info(bootAnalysisRunMsg);
                infoManager.refreshAnalysis(getServerUUID());
                this.cancel();
            }
        }).runTaskLaterAsynchronously(30 * TimeAmount.SECOND.ticks());

        bootAnalysisTaskID = bootAnalysisTask.getTaskId();

        if (analysisRefreshTaskIsEnabled) {
            RunnableFactory.createNew("PeriodicalAnalysisTask", new AbsRunnable() {
                @Override
                public void run() {
                    infoManager.refreshAnalysis(getServerUUID());
                }
            }).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }

        RunnableFactory.createNew("PeriodicNetworkBoxRefreshTask", new AbsRunnable() {
            @Override
            public void run() {
                infoManager.updateNetworkPageContent();
            }
        }).runTaskTimerAsynchronously(TimeAmount.SECOND.ticks(), TimeAmount.MINUTE.ticks() * 5L);

        Benchmark.stop("Enable", "Task Registration");
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

    /**
     * Disables the plugin.
     */
    @Override
    public void onDisable() {
        //Clears the page cache
        PageCache.clearCache();

        // Stop the UI Server
        if (webServer != null) {
            webServer.stop();
        }

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
        try {
            config.read();
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
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
     * Initializes the database according to settings in the config.
     * <p>
     * If database connection can not be established plugin is disabled.
     */
    private void initDatabase() throws DatabaseInitException {
        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();

        for (Database database : databases) {
            String databaseType = database.getConfigName().toLowerCase().trim();
            if (Verify.equalsIgnoreCase(dbType, databaseType)) {
                this.db = database;
                break;
            }
        }

        if (db == null) {
            throw new DatabaseInitException(Locale.get(Msg.ENABLE_FAIL_WRONG_DB).toString() + " " + dbType);
        }

        db.init();
        Log.info(Locale.get(Msg.ENABLE_DB_INFO).parse(db.getConfigName()));
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
    public Database getDB() {
        return db;
    }

    /**
     * Used to access WebServer.
     *
     * @return the WebServer
     */
    public WebServer getWebServer() {
        return webServer;
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
     * Used to get all possible database objects.
     * <p>
     * #init() might need to be called in order for the object to function.
     *
     * @return Set containing the SqLite and MySQL objects.
     */
    public Set<Database> getDatabases() {
        return databases;
    }

    /**
     * Used to get the ID of the BootAnalysisTask, so that it can be disabled.
     *
     * @return ID of the bootAnalysisTask
     */
    public int getBootAnalysisTaskID() {
        return bootAnalysisTaskID;
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

    public TPSCountTimer getTpsCountTimer() {
        return tpsCountTimer;
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
        return config;
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

    @Override
    public Theme getTheme() {
        return theme;
    }
}
