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
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.data.cache.PageCacheHandler;
import main.java.com.djrapitops.plan.data.listeners.*;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.MySQLDB;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.ui.html.Html;
import main.java.com.djrapitops.plan.ui.webserver.WebServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.metrics.BStats;
import org.apache.logging.log4j.LogManager;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
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

    private DataCacheHandler handler;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;
    private HookHandler hookHandler; // Manages 3rd party data sources

    private Database db;
    private Set<Database> databases;

    private WebServer uiServer;

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
    public static API getPlanAPI() throws IllegalStateException, NoClassDefFoundError {
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
        // Sets the Required variables for BukkitPlugin instance to function correctly
        setInstance(this);
        super.setDebugMode(Settings.DEBUG.toString());
        super.setColorScheme(new ColorScheme(Phrase.COLOR_MAIN.color(), Phrase.COLOR_SEC.color(), Phrase.COLOR_TER.color()));
        super.setLogPrefix("[Plan]");
        super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
        super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

        // Initializes BukkitPlugin variables, Checks version & Logs the debug header
        super.onEnableDefaultTasks();

        processStatus().startExecution("Enable");

        initLocale();
        Benchmark.start("Enable: Reading server variables");
        serverVariableHolder = new ServerVariableHolder(getServer());
        Benchmark.stop("Enable: Reading server variables");

        Benchmark.start("Enable: Copy default config");
        getConfig().options().copyDefaults(true);
        getConfig().options().header(Phrase.CONFIG_HEADER.toString());
        saveConfig();
        Benchmark.stop("Enable: Copy default config");

        Benchmark.start("Enable: Init Database");
        Log.info(Phrase.DB_INIT.toString());
        if (Check.ErrorIfFalse(initDatabase(), Phrase.DB_FAILURE_DISABLE.toString())) {
            Log.info(Phrase.DB_ESTABLISHED.parse(db.getConfigName()));
        } else {
            disablePlugin();
            return;
        }
        Benchmark.stop("Enable: Init Database");

        Benchmark.start("Enable: Init DataCache");
        this.handler = new DataCacheHandler(this);
        this.inspectCache = new InspectCacheHandler(this);
        this.analysisCache = new AnalysisCacheHandler(this);
        Benchmark.stop("Enable: Init DataCache");

        super.getRunnableFactory().createNew(new TPSCountTimer(this)).runTaskTimer(1000, TimeAmount.SECOND.ticks());
        registerListeners();

        this.api = new API(this);
        Benchmark.start("Enable: Handle Reload");
        handler.handleReload();
        Benchmark.stop("Enable: Handle Reload");

        Benchmark.start("Enable: Analysis refresh task registration");
        // Analysis refresh settings
        boolean bootAnalysisIsEnabled = Settings.ANALYSIS_REFRESH_ON_ENABLE.isTrue();
        int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
        boolean analysisRefreshTaskIsEnabled = analysisRefreshMinutes > 0;

        // Analysis refresh tasks
        if (bootAnalysisIsEnabled) {
            startBootAnalysisTask();
        }
        if (analysisRefreshTaskIsEnabled) {
            startAnalysisRefreshTask(analysisRefreshMinutes);
        }
        Benchmark.stop("Enable: Analysis refresh task registration");

        Benchmark.start("Enable: WebServer Initialization");
        // Data view settings
        boolean webserverIsEnabled = Settings.WEBSERVER_ENABLED.isTrue();
        boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        boolean usingAlternativeUI = Settings.USE_ALTERNATIVE_UI.isTrue();
        boolean hasDataViewCapability = usingAlternativeIP || usingAlternativeUI || webserverIsEnabled;

        if (webserverIsEnabled) {
            uiServer = new WebServer(this);
            uiServer.initServer();

            if (!uiServer.isEnabled()) {
                Log.error("WebServer was not successfully initialized.");
            }

            setupFilter();
        } else if (!hasDataViewCapability) {
            Log.infoColor(Phrase.ERROR_NO_DATA_VIEW.toString());
        }
        if (!usingAlternativeIP && serverVariableHolder.getIp().isEmpty()) {
            Log.infoColor(Phrase.NOTIFY_EMPTY_IP.toString());
        }
        Benchmark.stop("Enable: WebServer Initialization");

        registerCommand(new PlanCommand(this));

        Benchmark.start("Enable: Hook to 3rd party plugins");
        hookHandler = new HookHandler(this);
        Benchmark.stop("Enable: Hook to 3rd party plugins");

        BStats bStats = new BStats(this);
        bStats.registerMetrics();

        Log.debug("Verbose debug messages are enabled.");
        Log.info(Phrase.ENABLED.toString());
        processStatus().finishExecution("Enable");
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
            Log.info(Phrase.CACHE_SAVE.toString());

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
                taskStatus().cancelAllKnownTasks();
                Benchmark.stop("Disable: DataCache Save");
            });

            scheduler.shutdown(); // Schedules the save to shutdown after it has ran the execute method.
        }

        Log.info(Phrase.DISABLED.toString());
    }

    private void registerListeners() {
        Benchmark.start("Enable: Register Listeners");
        registerListener(new PlanPlayerListener(this));
        boolean chatListenerIsEnabled = Check.isTrue(Settings.GATHERCHAT.isTrue(), Phrase.NOTIFY_DISABLED_CHATLISTENER.toString());
        boolean commandListenerIsEnabled = Check.isTrue(Settings.GATHERCOMMANDS.isTrue(), Phrase.NOTIFY_DISABLED_COMMANDLISTENER.toString());
        boolean deathListenerIsEnabled = Check.isTrue(Settings.GATHERKILLS.isTrue(), Phrase.NOTIFY_DISABLED_DEATHLISTENER.toString());

        if (chatListenerIsEnabled) {
            registerListener(new PlanChatListener(this));
        }

        registerListener(new PlanGamemodeChangeListener(this));
        registerListener(new PlanWorldChangeListener(this));

        if (commandListenerIsEnabled) {
            registerListener(new PlanCommandPreprocessListener(this));
        }

        if (deathListenerIsEnabled) {
            registerListener(new PlanDeathEventListener(this));
        }

        Benchmark.stop("Enable: Register Listeners");
    }

    /**
     * Initializes the database according to settings in the config.
     * <p>
     * If database connection can not be established plugin is disabled.
     *
     * @return true if init was successful, false if not.
     */
    public boolean initDatabase() {
        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        String dbType = Settings.DB_TYPE.toString().toLowerCase().trim();

        for (Database database : databases) {
            String databaseType = database.getConfigName().toLowerCase().trim();
            Log.debug(databaseType + ": " + Verify.equalsIgnoreCase(dbType, databaseType));
            if (Verify.equalsIgnoreCase(dbType, databaseType)) {
                this.db = database;
                break;
            }
        }

        if (!Verify.notNull(db)) {
            Log.info(Phrase.DB_TYPE_DOES_NOT_EXIST.toString() + " " + dbType);
            return false;
        }

        return Check.ErrorIfFalse(db.init(), Phrase.DB_FAILURE_DISABLE.toString());
    }

    private void startAnalysisRefreshTask(int everyXMinutes) throws IllegalStateException {
        Benchmark.start("Enable: Schedule PeriodicAnalysisTask");
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
        Benchmark.stop("Enable: Schedule PeriodicAnalysisTask");
    }

    private void startBootAnalysisTask() throws IllegalStateException {
        Benchmark.start("Enable: Schedule boot analysis task");
        Log.info(Phrase.ANALYSIS_BOOT_NOTIFY + "");
        ITask bootAnalysisTask = getRunnableFactory().createNew("BootAnalysisTask", new AbsRunnable() {
            @Override
            public void run() {
                Log.debug("Running BootAnalysisTask");
                Log.info(Phrase.ANALYSIS_BOOT + "");
                analysisCache.updateCache();
                this.cancel();
            }
        }).runTaskLaterAsynchronously(30 * TimeAmount.SECOND.ticks());
        bootAnalysisTaskID = bootAnalysisTask.getTaskId();
        Benchmark.stop("Enable: Schedule boot analysis task");
    }

    /**
     * Used to write a new Locale file in the plugin's datafolder.
     */
    public void writeNewLocaleFile() {
        File genLocale = new File(getDataFolder(), "locale_EN.txt");
        try (
                FileWriter fw = new FileWriter(genLocale, true);
                PrintWriter pw = new PrintWriter(fw)
        ) {
            if (genLocale.createNewFile()) {
                Log.debug(genLocale.getAbsoluteFile() + " created");
            }

            for (Phrase p : Phrase.values()) {
                pw.println(p.name() + " <> " + p.parse());
                pw.flush();
            }
            pw.println("<<<<<<HTML>>>>>>");
            for (Html h : Html.values()) {
                pw.println(h.name() + " <> " + h.parse());
                pw.flush();
            }
        } catch (IOException ex) {
            Log.toLog(this.getClass().getName(), ex);
        }
    }

    private void initLocale() {
        String defaultLocale = "Default: EN";

        String locale = Settings.LOCALE.toString().toUpperCase();
        Benchmark.start("Enable: Initializing locale");
        File localeFile = new File(getDataFolder(), "locale.txt");

        String usingLocale;

        if (localeFile.exists()) {
            Phrase.loadLocale(localeFile);
            Html.loadLocale(localeFile);

            stopInitLocale(defaultLocale);
            return;
        }

        if (locale.equals("DEFAULT")) {
            stopInitLocale(defaultLocale);
            return;
        }

        String urlString = "https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/localization/locale_" + locale + ".txt";

        URL localeURL;
        try {
            localeURL = new URL(urlString);
        } catch (MalformedURLException e) {
            Log.error("Error at parsing \"" + urlString + "\" to an URL"); //Shouldn't ever happen

            stopInitLocale(defaultLocale);
            return;
        }

        try (InputStream inputStream = localeURL.openStream();
             OutputStream outputStream = new FileOutputStream(localeFile)) {

            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }

            Phrase.loadLocale(localeFile);
            Html.loadLocale(localeFile);
            usingLocale = locale;

            if (localeFile.delete()) {
                Log.debug(localeFile.getAbsoluteFile() + " (Locale File) deleted");
            }

            stopInitLocale(usingLocale);
        } catch (FileNotFoundException ex) {
            Log.error("Attempted using locale that doesn't exist.");

            stopInitLocale(defaultLocale);
        } catch (IOException e) {
            Log.error("Error at loading locale from GitHub, using default locale.");

            stopInitLocale(defaultLocale);
        }
    }

    /**
     * Setups the command console output filter
     */
    private void setupFilter() {
        org.apache.logging.log4j.core.Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        logger.addFilter(new RegisterCommandFilter());
    }

    /**
     * Stops initializing the locale
     *
     * @param usingLocale The locale that's used
     * @implNote Removes clutter in the method
     */
    private void stopInitLocale(String usingLocale) {
        Benchmark.stop("Enable: Initializing locale");
        Log.info("Using locale: " + usingLocale);
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
     * @return Current instance of the DataCacheHandler
     */
    public DataCacheHandler getHandler() {
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

    /**
     * Old method for getting the API.
     *
     * @return the Plan API.
     * @deprecated Use Plan.getPlanAPI() (static method) instead.
     */
    @Deprecated
    public API getAPI() {
        return api;
    }
}
