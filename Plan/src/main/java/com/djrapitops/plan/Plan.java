package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.api.API;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.*;
import com.djrapitops.plan.data.cache.*;
import com.djrapitops.plan.data.listeners.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.ui.Html;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/* TODO 2.2.0
Placeholder API
Database cleaning
Location Analysis to view meaningful locations on Dynmap (Investigate dynmap api)
Integrate PlanLite features to Plan and discontinue PlanLite
Seperate serverdata and userdata saving
Database Cleaning of useless data
Fix any bugs that come up
 */
/**
 *
 * @author Rsl1122
 */
public class Plan extends JavaPlugin {

    private API api;
    private PlanLiteHook planLiteHook;
    private DataCacheHandler handler;
    private InspectCacheHandler inspectCache;
    private AnalysisCacheHandler analysisCache;
    private Database db;
    private HashSet<Database> databases;
    private WebSocketServer uiServer;

    private int bootAnalysisTaskID;

    /**
     * OnEnable method.
     *
     * Creates the config file. Checks for new version. Initializes Database.
     * Hooks PlanLite. Initializes DataCaches. Registers Listeners. Registers
     * Command /plan and initializes API. Enables Webserver & analysis tasks if
     * enabled in config. Warns about possible mistakes made in config.
     */
    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        initLocale();

        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        getConfig().options().copyDefaults(true);
        getConfig().options().header(Phrase.CONFIG_HEADER + "");
        saveConfig();

        log(MiscUtils.checkVersion());

        log(Phrase.DB_INIT + "");
        if (initDatabase()) {
            log(Phrase.DB_ESTABLISHED.parse(db.getConfigName()));
        } else {
            logError(Phrase.DATABASE_FAILURE_DISABLE.toString());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        hookPlanLite();
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
                startBootRefreshTask();
            }
            int analysisRefreshMinutes = Settings.ANALYSIS_AUTO_REFRESH.getNumber();
            if (analysisRefreshMinutes != -1) {
                startAnalysisRefreshTask(analysisRefreshMinutes);
            }
        } else if (!(Settings.SHOW_ALTERNATIVE_IP.isTrue())
                || (Settings.USE_ALTERNATIVE_UI.isTrue()
                && planLiteHook.isEnabled())) {
            consoleSender.sendMessage(Phrase.PREFIX + "" + Phrase.ERROR_NO_DATA_VIEW);
        }
        if (!Settings.SHOW_ALTERNATIVE_IP.isTrue() && getServer().getIp().isEmpty()) {
            consoleSender.sendMessage(Phrase.NOTIFY_EMPTY_IP + "");
        }

        log(Phrase.ENABLED + "");
    }

    /**
     * Hooks PlanLite for UI and/or additional data.
     */
    public void hookPlanLite() {
        try {
            planLiteHook = new PlanLiteHook(this);
        } catch (NoClassDefFoundError | Exception e) {

        }
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
            log(Phrase.SAVE_CACHE + "");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
            });

            scheduler.shutdown();
        }
        log(Phrase.DISABLED + "");
    }

    /**
     * Logs the message to the console.
     *
     * @param message
     */
    public void log(String message) {
        getLogger().info(message);
    }

    /**
     * Logs an error message to the console.
     *
     * @param message
     */
    public void logError(String message) {
        getLogger().severe(message);
    }

    /**
     * @return Plan API
     */
    public API getAPI() {
        return api;
    }

    private void registerListeners() {
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlanChatListener(this), this);
        pluginManager.registerEvents(new PlanPlayerListener(this), this);
        pluginManager.registerEvents(new PlanGamemodeChangeListener(this), this);
        pluginManager.registerEvents(new PlanCommandPreprocessListener(this), this);
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
            log(Phrase.DATABASE_TYPE_DOES_NOT_EXIST.toString());
            return false;
        }

        if (!db.init()) {
            log(Phrase.DATABASE_FAILURE_DISABLE.toString());
            setEnabled(false);
            return false;
        }

        db.setVersion(0);

        return true;
    }

    private void startAnalysisRefreshTask(int analysisRefreshMinutes) throws IllegalStateException, IllegalArgumentException {
        (new BukkitRunnable() {
            @Override
            public void run() {
                if (!analysisCache.isCached()) {
                    analysisCache.updateCache();
                } else if (new Date().getTime() - analysisCache.getData().getRefreshDate() > 60000) {
                    analysisCache.updateCache();
                }
            }
        }).runTaskTimerAsynchronously(this, analysisRefreshMinutes * 60 * 20, analysisRefreshMinutes * 60 * 20);
    }

    private void startBootRefreshTask() throws IllegalStateException, IllegalArgumentException {
        log(Phrase.ANALYSIS_BOOT_NOTIFY + "");
        BukkitTask analysis = (new BukkitRunnable() {
            @Override
            public void run() {
                log(Phrase.ANALYSIS_BOOT + "");
                analysisCache.updateCache();
                this.cancel();
            }
        }).runTaskLater(this, 30 * 20);
        bootAnalysisTaskID = analysis.getTaskId();
    }

    /**
     * @return Currnet instance of the AnalysisCacheHandler
     */
    public AnalysisCacheHandler getAnalysisCache() {
        return analysisCache;
    }

    /**
     * @return Currnet instance of the InspectCacheHandler
     */
    public InspectCacheHandler getInspectCache() {
        return inspectCache;
    }

    /**
     * @return Currnet instance of the DataCacheHandler
     */
    public DataCacheHandler getHandler() {
        return handler;
    }

    /**
     * @return PlanLiteHook
     */
    public PlanLiteHook getPlanLiteHook() {
        return planLiteHook;
    }

    /**
     * @return the Database
     */
    public Database getDB() {
        return db;
    }

    /**
     * @return the Webserver
     */
    public WebSocketServer getUiServer() {
        return uiServer;
    }

    /**
     * @return Set containing SqLite & MySQL classes.
     */
    public HashSet<Database> getDatabases() {
        return databases;
    }

    /**
     * @return
     */
    public int getBootAnalysisTaskID() {
        return bootAnalysisTaskID;
    }

    private void initLocale() {
        String locale = Settings.LOCALE.toString().toUpperCase();
        File localeFile = new File(getDataFolder(), "locale.txt");
        File htmlLocale = new File(getDataFolder(), "htmlLocale.txt");
        boolean skipLoc = false;
        boolean skipHtmlLoc = false;
        String usingLocale = "";
        if (localeFile.exists()) {
            Phrase.loadLocale(localeFile);
            skipLoc = true;
            usingLocale = "locale.txt";
        }
        if (htmlLocale.exists()) {
            Html.loadLocale(htmlLocale);
            skipHtmlLoc = true;
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
                    usingLocale = locale;
                    localeFile.delete();
                }
                if (!skipHtmlLoc) {
                    URL localeURL = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/localization/htmlLocale_" + locale + ".txt");
                    InputStream inputStream = localeURL.openStream();
                    OutputStream outputStream = new FileOutputStream(htmlLocale);
                    int read = 0;
                    byte[] bytes = new byte[1024];
                    while ((read = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, read);
                    }
                    Html.loadLocale(htmlLocale);
                    htmlLocale.delete();
                }
            } catch (FileNotFoundException ex) {
                logError("Attempted using locale that doesn't exist.");
                usingLocale = "Default: EN";
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            usingLocale = "Default: EN";
        }
        log("Using locale: " + usingLocale);
    }
}
