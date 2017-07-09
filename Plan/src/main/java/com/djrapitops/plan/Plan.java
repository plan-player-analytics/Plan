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

import com.djrapitops.javaplugin.RslPlugin;
import com.djrapitops.javaplugin.api.ColorScheme;
import com.djrapitops.javaplugin.api.TimeAmount;
import com.djrapitops.javaplugin.task.runnable.RslRunnable;
import com.djrapitops.javaplugin.utilities.Verify;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.command.PlanCommand;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.cache.*;
import main.java.com.djrapitops.plan.data.listeners.*;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.*;
import main.java.com.djrapitops.plan.ui.Html;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;
import main.java.com.djrapitops.plan.utilities.Benchmark;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import com.djrapitops.javaplugin.task.ITask;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Javaplugin class that contains methods for starting the plugin, logging to
 * the Bukkit console and various get methods.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class Plan extends RslPlugin<Plan> {

    private API api;

    private DataCacheHandler handler;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;
    private HookHandler hookHandler;

    private Database db;
    private HashSet<Database> databases;

    private WebSocketServer uiServer;

    private ServerVariableHolder serverVariableHolder;
    private int bootAnalysisTaskID = -1;

    /**
     * OnEnable method.
     *
     * Creates the config file. Checks for new version. Initializes Database.
     * Hooks to Supported plugins. Initializes DataCaches. Registers Listeners.
     * Registers Command /plan and initializes API. Enables Webserver and
     * analysis tasks if enabled in config. Warns about possible mistakes made
     * in config.
     */
    @Override
    public void onEnable() {
        // Sets the Required variables for RslPlugin instance to function correctly
        setInstance(this);
        super.setDebugMode(Settings.DEBUG.toString());
        super.setColorScheme(new ColorScheme(Phrase.COLOR_MAIN.color(), Phrase.COLOR_SEC.color(), Phrase.COLOR_TER.color()));
        super.setLogPrefix("[Plan]");
        super.setUpdateCheckUrl("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
        super.setUpdateUrl("https://www.spigotmc.org/resources/plan-player-analytics.32536/");

        // Initializes RslPlugin variables, Checks version & Logs the debug header
        super.onEnableDefaultTasks();

        processStatus().startExecution("Enable");

        initLocale();
        Benchmark.start("Enable: Reading server variables");
        serverVariableHolder = new ServerVariableHolder(getServer());
        Benchmark.stop("Enable: Reading server variables");

        Benchmark.start("Enable: Copy default config");
        getConfig().options().copyDefaults(true);
        getConfig().options().header(Phrase.CONFIG_HEADER + "");
        saveConfig();
        Benchmark.stop("Enable: Copy default config");

        Benchmark.start("Enable: Init Database");
        Log.info(Phrase.DB_INIT + "");
        if (Check.ifTrue_Error(initDatabase(), Phrase.DB_FAILURE_DISABLE.toString())) {
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
        registerCommand(new PlanCommand(this));

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

        Benchmark.start("Enable: Webserver Initialization");
        // Data view settings
        boolean webserverIsEnabled = Settings.WEBSERVER_ENABLED.isTrue();
        boolean usingAlternativeIP = Settings.SHOW_ALTERNATIVE_IP.isTrue();
        boolean usingAlternativeUI = Settings.USE_ALTERNATIVE_UI.isTrue();
        boolean hasDataViewCapability = usingAlternativeIP || usingAlternativeUI || webserverIsEnabled;

        if (webserverIsEnabled) {
            uiServer = new WebSocketServer(this);
            uiServer.initServer();
        } else if (!hasDataViewCapability) {
            Log.infoColor(Phrase.ERROR_NO_DATA_VIEW + "");
        }
        if (!usingAlternativeIP && serverVariableHolder.getIp().isEmpty()) {
            Log.infoColor(Phrase.NOTIFY_EMPTY_IP + "");
        }
        Benchmark.stop("Enable: Webserver Initialization");

        Benchmark.start("Enable: Hook to 3rd party plugins");
        hookHandler = new HookHandler(this);
        Benchmark.stop("Enable: Hook to 3rd party plugins");

        Log.debug("Verboose debug messages are enabled.");
        Log.info(Phrase.ENABLED + "");
        processStatus().finishExecution("Enable");
    }

    /**
     * Disables the plugin.
     *
     * Stops the webserver, cancels all tasks and saves cache to the database.
     */
    @Override
    public void onDisable() {
        // Stop the UI Server
        if (uiServer != null) {
            uiServer.stop();
        }
        Bukkit.getScheduler().cancelTasks(this);
        if (Verify.notNull(handler, db)) {
            Benchmark.start("DataCache OnDisable Save");
            // Saves the datacache to the database without Bukkit's Schedulers.
            Log.info(Phrase.CACHE_SAVE + "");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
                taskStatus().cancelAllKnownTasks();
                Benchmark.stop("DataCache OnDisable Save");
            });
            scheduler.shutdown(); // Schedules the save to shutdown after it has ran the execute method.

        }
        Log.info(Phrase.DISABLED + "");
    }

    private void registerListeners() {
        Benchmark.start("Enable: Register Listeners");
        registerListener(new PlanPlayerListener(this));
        boolean chatListenerIsEnabled = Check.ifTrue(Settings.GATHERCHAT.isTrue(), Phrase.NOTIFY_DISABLED_CHATLISTENER + "");
        boolean gamemodeChangeListenerIsEnabled = Check.ifTrue(Settings.GATHERGMTIMES.isTrue(), Phrase.NOTIFY_DISABLED_GMLISTENER + "");
        boolean commandListenerIsEnabled = Check.ifTrue(Settings.GATHERCOMMANDS.isTrue(), Phrase.NOTIFY_DISABLED_COMMANDLISTENER + "");
        boolean deathListenerIsEnabled = Check.ifTrue(Settings.GATHERKILLS.isTrue(), Phrase.NOTIFY_DISABLED_DEATHLISTENER + "");

        if (chatListenerIsEnabled) {
            registerListener(new PlanChatListener(this));
        }
        if (gamemodeChangeListenerIsEnabled) {
            registerListener(new PlanGamemodeChangeListener(this));
        }
        if (commandListenerIsEnabled) {
            registerListener(new PlanCommandPreprocessListener(this));
        }
        if (deathListenerIsEnabled) {
            registerListener(new PlanDeathEventListener(this));
        }
        if (Settings.GATHERLOCATIONS.isTrue()) {
            registerListener(new PlanPlayerMoveListener(this));
        }
        Benchmark.stop("Enable: Register Listeners");
    }

    /**
     * Initializes the database according to settings in the config.
     *
     * If database connection can not be established plugin is disabled.
     *
     * @return true if init was successful, false if not.
     */
    public boolean initDatabase() {
        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        String dbType = (Settings.DB_TYPE + "").toLowerCase().trim();

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
        return Check.ifTrue_Error(db.init(), Phrase.DB_FAILURE_DISABLE.toString());
    }

    private void startAnalysisRefreshTask(int everyXMinutes) throws IllegalStateException {
        Benchmark.start("Enable: Schedule PeriodicAnalysisTask");
        if (everyXMinutes <= 0) {
            return;
        }
        getRunnableFactory().createNew("PeriodicalAnalysisTask", new RslRunnable() {
            @Override
            public void run() {
                Log.debug("Running PeriodicalAnalysisTask");
                if (!analysisCache.isCached()) {
                    analysisCache.updateCache();
                } else if (MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > TimeAmount.MINUTE.ms()) {
                    analysisCache.updateCache();
                }
            }
        }).runTaskTimerAsynchronously(everyXMinutes * TimeAmount.MINUTE.ticks(), everyXMinutes * TimeAmount.MINUTE.ticks());
        Benchmark.stop("Enable: Schedule PeriodicAnalysisTask");
    }

    private void startBootAnalysisTask() throws IllegalStateException {
        Benchmark.start("Enable: Schedule boot analysis task");
        Log.info(Phrase.ANALYSIS_BOOT_NOTIFY + "");
        ITask bootAnalysisTask = getRunnableFactory().createNew("BootAnalysisTask", new RslRunnable() {
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
        try {
            genLocale.createNewFile();
            FileWriter fw = new FileWriter(genLocale, true);
            PrintWriter pw = new PrintWriter(fw);
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
        String locale = Settings.LOCALE.toString().toUpperCase();
        Benchmark.start("Enable: Initializing locale");
        File localeFile = new File(getDataFolder(), "locale.txt");
        boolean skipLoc = false;
        String usingLocale = "";
        if (localeFile.exists()) {
            Phrase.loadLocale(localeFile);
            Html.loadLocale(localeFile);
            skipLoc = true;
            usingLocale = "locale.txt";
        }
        if (!locale.equals("DEFAULT")) {
            try {
                if (!skipLoc) {
                    URL localeURL = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/localization/locale_" + locale + ".txt");
                    InputStream inputStream = localeURL.openStream();
                    OutputStream outputStream = new FileOutputStream(localeFile);
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    Phrase.loadLocale(localeFile);
                    Html.loadLocale(localeFile);
                    usingLocale = locale;
                    localeFile.delete();
                }
            } catch (FileNotFoundException ex) {
                Log.error("Attempted using locale that doesn't exist.");
                usingLocale = "Default: EN";
            } catch (IOException e) {
            }
        } else {
            usingLocale = "Default: EN";
        }
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
    public WebSocketServer getUiServer() {
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
     *
     * #init() might need to be called in order for the object to function.
     *
     * @return Set containing the SqLite and MySQL objects.
     */
    public HashSet<Database> getDatabases() {
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
     * @deprecated Use Plan.getPlanAPI() (static method) instead.
     * @return the Plan API.
     */
    @Deprecated
    public API getAPI() {
        return api;
    }

    /**
     * Used to get the PlanAPI. @see API
     *
     * @return API of the current instance of Plan.
     * @throws IllegalStateException If onEnable method has not been called on
     * Plan and the instance is null.
     */
    public static API getPlanAPI() throws IllegalStateException {
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
}
