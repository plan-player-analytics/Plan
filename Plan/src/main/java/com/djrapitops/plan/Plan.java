package com.djrapitops.plan;

import com.djrapitops.plan.command.PlanCommand;
import com.djrapitops.plan.api.API;
import com.djrapitops.plan.data.cache.AnalysisCacheHandler;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.MySQLDB;
import com.djrapitops.plan.database.databases.SQLiteDB;
import com.djrapitops.plan.data.cache.DataCacheHandler;
import com.djrapitops.plan.data.cache.InspectCacheHandler;
import com.djrapitops.plan.data.listeners.*;
import main.java.com.djrapitops.plan.ui.webserver.WebSocketServer;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

/* TODO 2.1.0
Placeholder API
Immutable InspectCache ?
Recent players
Optimize db with batch processing (commanduse, ips, nicks)
Manage command
Database cleaning
PlanLite Top 20 richest
PlanLite Top 20 most votes
Top 20 most active
Clear setting multiper (InspectCache)
Clear check for existing clear task. (InspectCache)
    Update Version checker so it doesn't fail
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

    /**
     * OnEnable method.
     * 
     * Initiates the plugin with database, webserver, commands & listeners.
     */
    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        databases = new HashSet<>();
        databases.add(new MySQLDB(this));
        databases.add(new SQLiteDB(this));

        for (Database database : databases) {
            String name = database.getConfigName();

            ConfigurationSection section = getConfig().getConfigurationSection(name);

            if (section == null) {
                section = getConfig().createSection(name);
            }

            database.getConfigDefaults(section);

            if (section.getKeys(false).isEmpty()) {
                getConfig().set(name, null);
            }
        }

        getConfig().options().copyDefaults(true);

        getConfig().options().header("Plan Config | More info at https://www.spigotmc.org/wiki/plan-configuration/");

        saveConfig();

        log("Database init..");
        initDatabase();
        log("Database initiated.");

        hookPlanLite();
        this.handler = new DataCacheHandler(this);
        this.inspectCache = new InspectCacheHandler(this);
        this.analysisCache = new AnalysisCacheHandler(this);
        registerListeners();

        log(MiscUtils.checkVersion());

        getCommand("plan").setExecutor(new PlanCommand(this));

        this.api = new API(this);
        handler.handleReload();

        if (getConfig().getBoolean("Settings.WebServer.Enabled")) {
            uiServer = new WebSocketServer(this);
            uiServer.initServer();
            if (getConfig().getBoolean("Settings.Cache.AnalysisCache.RefreshAnalysisCacheOnEnable")) {
                log("Analysis | Boot analysis in 30 seconds..");
                (new BukkitRunnable() {
                    @Override
                    public void run() {
                        log("Analysis | Starting Boot Analysis..");
                        analysisCache.updateCache();
                        this.cancel();
                    }
                }).runTaskLater(this, 30 * 20);
            }
        } else if (!(getConfig().getBoolean("Settings.WebServer.ShowAlternativeServerIP")
                || (getConfig().getBoolean("Settings.PlanLite.UseAsAlternativeUI")
                && planLiteHook.isEnabled()))) {
            Bukkit.getServer().getConsoleSender().sendMessage("[Plan] "
                    + Phrase.ERROR_NO_DATA_VIEW);
        }
        log("Player Analytics Enabled.");
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
     * Stops the webserver, cancels all tasks and saves cache to the database.     * 
     */
    @Override
    public void onDisable() {
        if (uiServer != null) {
            uiServer.stop();
        }
        Bukkit.getScheduler().cancelTasks(this);
        if (handler != null) {
            log("Saving cached data..");
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            scheduler.execute(() -> {
                handler.saveCacheOnDisable();
            });

            scheduler.shutdown();
        }
        log("Player Analytics Disabled.");
    }

    /**
     * Logs the message to the console.
     * @param message
     */
    public void log(String message) {
        getLogger().info(message);
    }

    /**
     * Logs an error message to the console.
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
        getServer().getPluginManager().registerEvents(new PlanChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanGamemodeChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanCommandPreprocessListener(this), this);
        // Locations Removed from Build 2.0.0 for performance reasons.
        // getServer().getPluginManager().registerEvents(new PlanPlayerMoveListener(this), this);
    }

    private boolean initDatabase() {
        String type = getConfig().getString("database.type");

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
}
