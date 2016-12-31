package com.djrapitops.plan;

import com.djrapitops.plan.api.API;
import com.djrapitops.plan.api.Hook;
import com.djrapitops.plan.command.utils.MiscUtils;
import com.djrapitops.plan.database.Database;
import com.djrapitops.plan.database.databases.MySQLDB;
import com.djrapitops.plan.database.databases.SQLiteDB;
import com.djrapitops.plan.datahandlers.DataHandler;
import com.djrapitops.plan.datahandlers.listeners.PlanChatListener;
import com.djrapitops.plan.datahandlers.listeners.PlanCommandPreprocessListener;
import com.djrapitops.plan.datahandlers.listeners.PlanGamemodeChangeListener;
import com.djrapitops.plan.datahandlers.listeners.PlanPlayerListener;
import com.djrapitops.plan.datahandlers.listeners.PlanPlayerMoveListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import org.bukkit.configuration.ConfigurationSection;

public class Plan extends JavaPlugin {

    private API api;
    private PlanLiteHook planLiteHook;
    private DataHandler handler;
    private Database db;
    private HashSet<Database> databases;

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

        getConfig().options().header("Plan Config\n"
                + "debug - Errors are saved in errorlog.txt when they occur\n"
                + "visible - Plugin's data is accessable with /plan inspect command"
        );

        saveConfig();

        initDatabase();

        this.api = new API(this);
        hookPlanLite();
        this.handler = new DataHandler(this);
        registerListeners();

        log(MiscUtils.checkVersion());

        getCommand("plan").setExecutor(new PlanCommand(this));
        handler.handleReload();

        logToFile("-- Server Start/Reload --");
        log("Player Analytics Enabled.");
    }

    public void hookPlanLite() {
        try {
            if (getConfig().getBoolean("enabledData.planLite.pluginEnabled")) {
                planLiteHook = new PlanLiteHook(this);
            }
        } catch (NoClassDefFoundError | Exception e) {
            
        }
    }

    @Deprecated
    public List<String> hookInit() {
        return new ArrayList<>();
    }

    @Override
    public void onDisable() {
        log("Saving cached data..");
        handler.saveCacheOnDisable();
        db.close();
        log("Player Analytics Disabled.");
    }

    public void log(String message) {
        getLogger().info(message);
    }

    public void logError(String message) {
        getLogger().severe(message);
    }

    public void logToFile(String message) {
        if (getConfig().getBoolean("debug")) {
            File folder = getDataFolder();
            if (!folder.exists()) {
                folder.mkdir();
            }
            File log = new File(getDataFolder(), "Errors.txt");
            try {
                if (!log.exists()) {
                    log.createNewFile();
                }
                FileWriter fw = new FileWriter(log, true);
                try (PrintWriter pw = new PrintWriter(fw)) {
                    pw.println(message + "\n");
                    pw.flush();
                }
            } catch (IOException e) {
                logError("Failed to create Errors.txt file");
            }
        }
    }

    public API getAPI() {
        return api;
    }

    public void addExtraHook(String name, Hook hook) {
        if (planLiteHook != null) {
            planLiteHook.addExtraHook(name, hook);
        } else {
            logError(Phrase.ERROR_PLANLITE.toString());
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlanChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanGamemodeChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanCommandPreprocessListener(this), this);
        getServer().getPluginManager().registerEvents(new PlanPlayerMoveListener(this), this);
    }

    public DataHandler getHandler() {
        return handler;
    }

    public PlanLiteHook getPlanLiteHook() {
        return planLiteHook;
    }

    public Database getDB() {
        return db;
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

        return true;
    }
}
