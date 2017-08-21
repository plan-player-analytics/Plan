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
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.command.PlanCommand;
import main.java.com.djrapitops.plan.command.commands.RegisterCommandFilter;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import main.java.com.djrapitops.plan.data.cache.DataCache;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.data.listeners.*;
import main.java.com.djrapitops.plan.data.server.ServerInfoManager;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.queue.processing.ProcessingQueue;
import main.java.com.djrapitops.plan.queue.processing.Processor;
import main.java.com.djrapitops.plan.ui.webserver.WebServer;
import main.java.com.djrapitops.plan.ui.webserver.api.bukkit.*;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.webserver.api.WebAPIManager;
import org.apache.logging.log4j.LogManager;
import org.bukkit.ChatColor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main class for Bukkit that manages the plugin.
 * <p>
 * Everything can be accessed through this class. Use Plan.getInstance() to get
 * the initialised instance of Plan.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class Plan extends BukkitPlugin<Plan> {

    private API api;

    private ProcessingQueue processingQueue;
    private DataCache handler;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;
    private HookHandler hookHandler; // Manages 3rd party data sources

    private Database db;
    private Set<Database> databases;

    private WebServer uiServer;

    private ServerInfoManager serverInfoManager;

    private ServerVariableHolder serverVariableHolder;
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
        return instance.api;
    }

    /**
     * Used to get the plugin-instance singleton.
     *
     * @return this object.
     */
    public static Plan getInstance() {
        return (Plan) getPluginInstance(Plan.class);
    }

    /**
     * OnEnable method.
     * <p>
     * - Enables the plugin's subsystems.
     */
    @Override
    public void onEnable() {
        try {
            // Sets the Required variables for BukkitPlugin instance to function correctly
            setInstance(this);
            super.setDebugMode(Settings.DEBUG.toString());
            initColorScheme();
            super.setLogPrefix("[Plan]");
            super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
            super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

            // Initializes BukkitPlugin variables, Checks version & Logs the debug header
            super.onEnableDefaultTasks();

            Benchmark.start("Enable");

            // Initialize Locale
            new Locale(this).loadLocale();

            Benchmark.start("Reading server variables");
            serverVariableHolder = new ServerVariableHolder(getServer());
            Benchmark.stop("Enable", "Reading server variables");

            Benchmark.start("Copy default config");
            getConfig().options().copyDefaults(true);
            getConfig().options().header("Plan Config | More info at https://www.spigotmc.org/wiki/plan-configuration/");
            saveConfig();
            Benchmark.stop("Enable", "Copy default config");

            processingQueue = new ProcessingQueue();

            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            if (Check.errorIfFalse(initDatabase(), Locale.get(Msg.ENABLE_DB_FAIL_DISABLE_INFO).toString())) {
                Log.info(Locale.get(Msg.ENABLE_DB_INFO).parse(db.getConfigName()));
            } else {
                disablePlugin();
                return;
            }
            Benchmark.stop("Enable", "Init Database");

            Benchmark.start("Init DataCache");
            this.handler = new DataCache(this);
            this.inspectCache = new InspectCacheHandler(this);
            this.analysisCache = new AnalysisCacheHandler(this);
            Benchmark.stop("Enable", "Init DataCache");

            super.getRunnableFactory().createNew(new TPSCountTimer(this)).runTaskTimer(1000, TimeAmount.SECOND.ticks());
            registerListeners();

            this.api = new API(this);

            Benchmark.start("Analysis refresh task registration");
            // Analysis refresh settings
            int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
            boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;

            // Analysis refresh tasks
            startBootAnalysisTask();
            if (analysisRefreshTaskIsEnabled) {
                startAnalysisRefreshTask(analysisRefreshMinutes);
            }

            Benchmark.stop("Enable", "Analysis refresh task registration");

            Benchmark.start("WebServer Initialization");

            uiServer = new WebServer(this);
            registerWebAPIs(); // TODO Move to WebServer class
            uiServer.initServer();

            if (!uiServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized.");
            }

            Benchmark.start("ServerInfo Registration");
            serverInfoManager = new ServerInfoManager(this);
            Benchmark.stop("Enable", "ServerInfo Registration");

            setupFilter(); // TODO Move to RegisterCommand Constructor

            // Data view settings // TODO Rewrite. (TextUI removed & webserver might be running on bungee
            boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
            boolean hasDataViewCapability = usingAlternativeIP;

            if (!hasDataViewCapability) {
                Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_NO_DATA_VIEW).toString());
            }
            if (!usingAlternativeIP && serverVariableHolder.getIp().isEmpty()) {
                Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_EMPTY_IP).toString());
            }

            Benchmark.stop("Enable", "WebServer Initialization");

            registerCommand(new PlanCommand(this));

            Benchmark.start("Hook to 3rd party plugins");
            hookHandler = new HookHandler(this);
            Benchmark.stop("Enable", "Hook to 3rd party plugins");

//Analytics temporarily disabled TODO enable before release
//            BStats bStats = new BStats(this);
//            bStats.registerMetrics();

            Log.debug("Verbose debug messages are enabled.");
            Log.logDebug("Enable", Benchmark.stop("Enable", "Enable"));
            Log.info(Locale.get(Msg.ENABLED).toString());
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.toLog(this.getClass().getName(), e);
            disablePlugin();
        }
    }

    private void initColorScheme() {
        try {
            ChatColor mainColor = ChatColor.getByChar(Settings.COLOR_MAIN.toString().charAt(1));
            ChatColor secColor = ChatColor.getByChar(Settings.COLOR_SEC.toString().charAt(1));
            ChatColor terColor = ChatColor.getByChar(Settings.COLOR_TER.toString().charAt(1));
            super.setColorScheme(new ColorScheme(mainColor, secColor, terColor));
        } catch (Exception e) {
            Log.infoColor(ChatColor.RED + "Customization, Chat colors set-up wrong, using defaults.");
            super.setColorScheme(new ColorScheme(ChatColor.DARK_GREEN, ChatColor.GRAY, ChatColor.WHITE));
        }
    }

    /**
     * Disables the plugin.
     * <p>
     * Stops the webserver, cancels all tasks and saves cache to the database.
     */
    @Override
    public void onDisable() {
        //Clears the page cache
        PageCacheHandler.clearCache();

        // Stop the UI Server
        if (uiServer != null) {
            uiServer.stop();
        }

        getServer().getScheduler().cancelTasks(this);

        if (Verify.notNull(handler, db)) {
            Benchmark.start("Disable: DataCache Save");
            // Saves the DataCache to the database without Bukkit's Schedulers.
            Log.info(Locale.get(Msg.DISABLE_CACHE_SAVE).toString());

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
                taskStatus().cancelAllKnownTasks();
                Benchmark.stop("Disable: DataCache Save");
            });

            scheduler.shutdown(); // Schedules the save to shutdown after it has ran the execute method.
        }

        getPluginLogger().endAllDebugs();
        Log.info(Locale.get(Msg.DISABLED).toString());
//        Locale.unload();
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

    private void registerWebAPIs() {
        WebAPIManager.registerNewAPI("analytics", new AnalyticsWebAPI());
        WebAPIManager.registerNewAPI("analyze", new AnalyzeWebAPI());
        WebAPIManager.registerNewAPI("configure", new ConfigureWebAPI());
        WebAPIManager.registerNewAPI("inspection", new InspectionWebAPI());
        WebAPIManager.registerNewAPI("inspect", new InspectWebAPI());
    }

    /**
     * Initializes the database according to settings in the config.
     * <p>
     * If database connection can not be established plugin is disabled.
     *
     * @return true if init was successful, false if not.
     */
    private boolean initDatabase() {
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

        if (!Verify.notNull(db)) {
            Log.info(Locale.get(Msg.ENABLE_FAIL_WRONG_DB).toString() + " " + dbType);
            return false;
        }

        return Check.errorIfFalse(db.init(), Locale.get(Msg.ENABLE_DB_FAIL_DISABLE_INFO).toString());
    }

    private void startAnalysisRefreshTask(int everyXMinutes) {
        Benchmark.start("Schedule PeriodicAnalysisTask");
        if (everyXMinutes <= 0) {
            return;
        }
        getRunnableFactory().createNew("PeriodicalAnalysisTask", new AbsRunnable() {
            @Override
            public void run() {
                Log.debug("Running PeriodicalAnalysisTask");
                if (!analysisCache.isCached() || MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > TimeAmount.MINUTE.ms()) {
                    analysisCache.updateCache();
                }
            }
        }).runTaskTimerAsynchronously(everyXMinutes * TimeAmount.MINUTE.ticks(), everyXMinutes * TimeAmount.MINUTE.ticks());
        Benchmark.stop("Schedule PeriodicAnalysisTask");
    }

    private void startBootAnalysisTask() {
        Benchmark.start("Schedule boot analysis task");
        String bootAnalysisMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_INFO).toString();
        String bootAnalysisRunMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_RUN_INFO).toString();

        Log.info(bootAnalysisMsg);

        ITask bootAnalysisTask = getRunnableFactory().createNew("BootAnalysisTask", new AbsRunnable() {
            @Override
            public void run() {
                Log.debug("Running BootAnalysisTask");
                Log.info(bootAnalysisRunMsg);

                analysisCache.updateCache();
                this.cancel();
            }
        }).runTaskLaterAsynchronously(30 * TimeAmount.SECOND.ticks());
        bootAnalysisTaskID = bootAnalysisTask.getTaskId();
        Benchmark.stop("Enable", "Schedule boot analysis task");
    }

    /**
     * Setups the command console output filter
     */
    private void setupFilter() {
        org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        logger.addFilter(new RegisterCommandFilter());
    }

    /**
     * Used to access AnalysisCache.
     *
     * @return Current instance of the AnalysisCacheHandler
     */
    public AnalysisCacheHandler getAnalysisCache() {
        return analysisCache;
    }

    /**
     * Used to access InspectCache.
     *
     * @return Current instance of the InspectCacheHandler
     */
    public InspectCacheHandler getInspectCache() {
        return inspectCache;
    }

    /**
     * Used to access Cache.
     *
     * @return Current instance of the DataCache
     */
    public DataCache getHandler() {
        return handler;
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
     * Used to access Webserver.
     *
     * @return the Webserver
     */
    public WebServer getUiServer() {
        return uiServer;
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

    public ProcessingQueue getProcessingQueue() {
        return processingQueue;
    }

    public void addToProcessQueue(Processor processor) {
        processingQueue.addToQueue(processor);
    }
}
