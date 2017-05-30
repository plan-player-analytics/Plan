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
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Javaplugin class that contains methods for starting the plugin, logging to
 * the Bukkit console and various get methods.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class Plan extends JavaPlugin {

    private API api;
    private DataCacheHandler handler;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;
    private Database db;
    private HashSet<Database> databases;
    private WebSocketServer uiServer;
    private HookHandler hookHandler;

    private int bootAnalysisTaskID;

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
        setInstance(this);
        getDataFolder().mkdirs();

        initLocale();

        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        getConfig().options().copyDefaults(true);
        getConfig().options().header(Phrase.CONFIG_HEADER + "");
        saveConfig();

        Log.info(MiscUtils.checkVersion());

        Log.info(Phrase.DB_INIT + "");
        if (initDatabase()) {
            Log.info(Phrase.DB_ESTABLISHED.parse(db.getConfigName()));
        } else {
            Log.error(Phrase.DB_FAILURE_DISABLE.toString());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.handler = new DataCacheHandler(this);
        this.inspectCache = new InspectCacheHandler(this);
        this.analysisCache = new AnalysisCacheHandler(this);
        registerListeners();

        getCommand("plan").setExecutor(new PlanCommand(this));

        this.api = new API(this);
        handler.handleReload();
        ConsoleCommandSender consoleSender = getServer().getConsoleSender();

        bootAnalysisTaskID = -1;
        if (Settings.WEBSERVER_ENABLED.isTrue()) {
            uiServer = new WebSocketServer(this);
            uiServer.initServer();
            if (Settings.ANALYSIS_REFRESH_ON_ENABLE.isTrue()) {
                startBootAnalysisTask();
            }
            int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
            if (analysisRefreshMinutes != -1) {
                startAnalysisRefreshTask(analysisRefreshMinutes);
            }
        } else if (!(Settings.SHOW_ALTERNATIVE_IP.isTrue())
                || (Settings.USE_ALTERNATIVE_UI.isTrue())) {
            consoleSender.sendMessage(Phrase.PREFIX + "" + Phrase.ERROR_NO_DATA_VIEW);
        }
        if (!Settings.SHOW_ALTERNATIVE_IP.isTrue() && getServer().getIp().isEmpty()) {
            consoleSender.sendMessage(Phrase.NOTIFY_EMPTY_IP + "");
        }

        hookHandler = new HookHandler();
        Log.debug("Verboose debug messages are enabled.");
        Log.info(Phrase.ENABLED + "");
    }

    /**
     * Disables the plugin.
     *
     * Stops the webserver, cancels all tasks and saves cache to the database.
     */
    @Override
    public void onDisable() {
        if (uiServer != null) {
            uiServer.stop();
        }
        Bukkit.getScheduler().cancelTasks(this);
        if (handler != null) {
            Log.info(Phrase.CACHE_SAVE + "");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
            });

            scheduler.shutdown();
        }
        Log.info(Phrase.DISABLED + "");
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlanChatListener(this), this);
        pluginManager.registerEvents(new PlanPlayerListener(this), this);
        pluginManager.registerEvents(new PlanGamemodeChangeListener(this), this);
        pluginManager.registerEvents(new PlanCommandPreprocessListener(this), this);
        pluginManager.registerEvents(new PlanDeathEventListener(this), this);
        if (Settings.GATHERLOCATIONS.isTrue()) {
            pluginManager.registerEvents(new PlanPlayerMoveListener(this), this);
        }
    }

    /**
     * Initializes the database according to settings in the config.
     *
     * If database connection can not be established plugin is disabled.
     *
     * @return true if init was successful, false if not.
     */
    public boolean initDatabase() {
        String type = Settings.DB_TYPE + "";

        db = null;
        for (Database database : databases) {
            if (type.equalsIgnoreCase(database.getConfigName())) {
                this.db = database;

                break;
            }
        }
        if (db == null) {
            Log.info(Phrase.DB_TYPE_DOES_NOT_EXIST.toString());
            return false;
        }
        if (!db.init()) {
            Log.info(Phrase.DB_FAILURE_DISABLE.toString());
            setEnabled(false);
            return false;
        }

        return true;
    }

    private void startAnalysisRefreshTask(int analysisRefreshMinutes) throws IllegalStateException, IllegalArgumentException {
        BukkitTask asyncPeriodicalAnalysisTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!analysisCache.isCached()) {
                    analysisCache.updateCache();
                } else if (MiscUtils.getTime() - analysisCache.getData().getRefreshDate() > 60000) {
                    analysisCache.updateCache();
                }
            }
        }.runTaskTimerAsynchronously(this, analysisRefreshMinutes * 60 * 20, analysisRefreshMinutes * 60 * 20);
    }

    private void startBootAnalysisTask() throws IllegalStateException, IllegalArgumentException {
        Log.info(Phrase.ANALYSIS_BOOT_NOTIFY + "");
        BukkitTask bootAnalysisTask = new BukkitRunnable() {
            @Override
            public void run() {
                Log.info(Phrase.ANALYSIS_BOOT + "");
                analysisCache.updateCache();
                this.cancel();
            }
        }.runTaskLater(this, 30 * 20);
        bootAnalysisTaskID = bootAnalysisTask.getTaskId();
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
     * Old method for getting the API.
     *
     * @deprecated Use Plan.getAPI() (static method) instead.
     * @return the Plan API.
     */
    @Deprecated
    public API getAPI() {
        return api;
    }

    private void initLocale() {
        String locale = Settings.LOCALE.toString().toUpperCase();
        /*// Used to write a new Locale file
        File genLocale = new File(getDataFolder(), "locale_EN.txt");
        try {
            genLocale.createNewFile();
            FileWriter fw = new FileWriter(genLocale, true);
            PrintWriter pw = new PrintWriter(fw);
            for (Phrase p : Phrase.values()) {                
                pw.println(p.name()+" <> "+p.parse());
                pw.flush();            
            }
            pw.println("<<<<<<HTML>>>>>>");
            for (Html h : Html.values()) {                
                pw.println(h.name()+" <> "+h.parse());
                pw.flush();            
            }
        } catch (IOException ex) {
            Logger.getLogger(Plan.class.getName()).log(Level.SEVERE, null, ex);
        }*/
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
        Log.info("Using locale: " + usingLocale);
    }

    /**
     * Used to get the current instance of Plan.
     *
     * Instance is set on the first line of onEnable method.
     *
     * @return current instance of Plan, Singleton.
     * @throws IllegalStateException If onEnable method has not been called and
     * the instance is null.
     */
    public static Plan getInstance() {
        Plan INSTANCE = PlanHolder.INSTANCE;
        if (INSTANCE == null) {
            throw new IllegalStateException("Plugin not enabled properly, Singleton instance is null.");
        }
        return INSTANCE;
    }
    
    /**
     * Used to set the current instance of Plan.
     *
     * @param plan The newly enabled Plan instance.
     */
    public static void setInstance(Plan plan) {
        PlanHolder.INSTANCE = plan;
    }

    /**
     * Used to get the PlanAPI. @see API
     *
     * @return API of the current instance of Plan.
     * @throws IllegalStateException If onEnable method has not been called on
     * Plan and the instance is null.
     */
    public static API getPlanAPI() throws IllegalStateException {
        Plan INSTANCE = PlanHolder.INSTANCE;
        if (INSTANCE == null) {
            throw new IllegalStateException("Plugin not enabled properly, Singleton instance is null.");
        }
        return INSTANCE.api;
    }

    private static class PlanHolder {

        private static Plan INSTANCE = null;
    }
}
