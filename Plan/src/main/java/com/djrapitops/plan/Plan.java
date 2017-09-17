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
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.api.exceptions.DatabaseInitException;
import main.java.com.djrapitops.plan.command.PlanCommand;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.cache.DataCache;
import main.java.com.djrapitops.plan.systems.cache.GeolocationCache;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.info.ImporterManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfoManager;
import main.java.com.djrapitops.plan.systems.listeners.*;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.processing.importing.importers.OfflinePlayerImporter;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.systems.webserver.PageCache;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.metrics.BStats;
import org.bukkit.ChatColor;

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
public class Plan extends BukkitPlugin<Plan> implements IPlan {

    private API api;

    private ProcessingQueue processingQueue;
    private HookHandler hookHandler; // Manages 3rd party data sources

    private Database db;
    private Set<Database> databases;

    private WebServer webServer;

    private InformationManager infoManager;
    private ServerInfoManager serverInfoManager;

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

    public static UUID getServerUUID() {
        return getInstance().getServerInfoManager().getServerUUID();
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

            GeolocationCache.checkDB();

            new Locale(this).loadLocale();

            Benchmark.start("Reading server variables");
            serverVariableHolder = new ServerVariableHolder(getServer());
            Benchmark.stop("Enable", "Reading server variables");

            Benchmark.start("Copy default config");
            getConfig().options().copyDefaults(true);
            getConfig().options().header("Plan Config | More info at https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/documentation/Configuration.md");
            saveConfig();
            Benchmark.stop("Enable", "Copy default config");


            Benchmark.start("Init Database");
            Log.info(Locale.get(Msg.ENABLE_DB_INIT).toString());
            initDatabase();
            Benchmark.stop("Enable", "Init Database");

            Benchmark.start("WebServer Initialization");
            webServer = new WebServer(this);

            processingQueue = new ProcessingQueue();

            serverInfoManager = new ServerInfoManager(this);
            infoManager = new BukkitInformationManager(this);

            webServer.initServer();
            if (!webServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized.");
            }

            Benchmark.stop("Enable", "WebServer Initialization");

            registerListeners();
            registerTasks();

            this.api = new API(this);

            // Data view settings // TODO Rewrite. (TextUI removed & webServer might be running on bungee
//            boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
//            boolean hasDataViewCapability = usingAlternativeIP;
//
//            if (!hasDataViewCapability) {
//                Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_NO_DATA_VIEW).toString());
//            }
//            if (!usingAlternativeIP && serverVariableHolder.getIp().isEmpty()) {
//                Log.infoColor(Locale.get(Msg.ENABLE_NOTIFY_EMPTY_IP).toString());
//            }

            registerCommand(new PlanCommand(this));

            Benchmark.start("Hook to 3rd party plugins");
            hookHandler = new HookHandler(this);
            Benchmark.stop("Enable", "Hook to 3rd party plugins");

            ImporterManager.registerImporter(new OfflinePlayerImporter());

            BStats bStats = new BStats(this);
            bStats.registerMetrics();

            Log.debug("Verbose debug messages are enabled.");
            Log.logDebug("Enable", Benchmark.stop("Enable", "Enable"));
            Log.info(Locale.get(Msg.ENABLED).toString());
            new ShutdownHook(this);
        } catch (Exception e) {
            Log.error("Plugin Failed to Initialize Correctly.");
            Log.logStackTrace(e);
            disablePlugin();
        }
    }

    private void registerTasks() {
        RunnableFactory runnableFactory = getRunnableFactory();
        String bootAnalysisMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_INFO).toString();
        String bootAnalysisRunMsg = Locale.get(Msg.ENABLE_BOOT_ANALYSIS_RUN_INFO).toString();

        Benchmark.start("Task Registration");
        tpsCountTimer = new TPSCountTimer(this);
        runnableFactory.createNew(tpsCountTimer).runTaskTimer(1000, TimeAmount.SECOND.ticks());

        // Analysis refresh settings
        int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;
        long analysisPeriod = analysisRefreshMinutes * TimeAmount.MINUTE.ticks();

        Log.info(bootAnalysisMsg);

        ITask bootAnalysisTask = runnableFactory.createNew("BootAnalysisTask", new AbsRunnable() {
            @Override
            public void run() {
                Log.info(bootAnalysisRunMsg);
                infoManager.refreshAnalysis();
                this.cancel();
            }
        }).runTaskLaterAsynchronously(30 * TimeAmount.SECOND.ticks());

        bootAnalysisTaskID = bootAnalysisTask.getTaskId();

        if (analysisRefreshTaskIsEnabled) {
            runnableFactory.createNew("PeriodicalAnalysisTask", new AbsRunnable() {
                @Override
                public void run() {
                    infoManager.refreshAnalysis();
                }
            }).runTaskTimerAsynchronously(analysisPeriod, analysisPeriod);
        }

        Benchmark.stop("Enable", "Task Registration");
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
            Log.info("Processing unprocessed processors. (" + processors.size() + ")"); // TODO Move to Locale
            for (Processor processor : processors) {
                processor.process();
            }
        }

        getServer().getScheduler().cancelTasks(this);

        if (Verify.notNull(infoManager, db)) {
            taskStatus().cancelAllKnownTasks();
        }

        getPluginLogger().endAllDebugs();
        Log.info(Locale.get(Msg.DISABLED).toString());
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
     *
     * @return true if init was successful, false if not.
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
     * @return ServerInfoManager
     * @see ServerInfoManager
     */
    public ServerInfoManager getServerInfoManager() {
        return serverInfoManager;
    }

    public ProcessingQueue getProcessingQueue() {
        return processingQueue;
    }

    public TPSCountTimer getTpsCountTimer() {
        return tpsCountTimer;
    }

    public void addToProcessQueue(Processor... processors) {
        for (Processor processor : processors) {
            processingQueue.addToQueue(processor);
        }
    }

    public InformationManager getInfoManager() {
        return infoManager;
    }
}
