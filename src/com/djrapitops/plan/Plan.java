package com.djrapitops.plan;

import com.djrapitops.plan.command.hooks.EssentialsHook;
import com.djrapitops.plan.command.hooks.FactionsHook;
import com.djrapitops.plan.command.hooks.OnTimeHook;
import com.djrapitops.plan.command.hooks.Hook;
import com.djrapitops.plan.command.hooks.PlaceholderAPIHook;
import com.djrapitops.plan.command.hooks.SuperbVoteHook;
//import com.djrapitops.plan.command.hooks.McMMOHook;
import com.djrapitops.plan.command.hooks.TownyHook;
import com.djrapitops.plan.command.hooks.VaultHook;
import com.djrapitops.plan.command.hooks.AdvancedAchievementsHook;
import com.djrapitops.plan.command.utils.DataUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Plan extends JavaPlugin {

    private final Map<String, Hook> hooks;
    private Hook placeholderAPIHook;
    private API api;
    private final Map<String, Hook> extraHooks;

    public Plan() {
        this.hooks = new HashMap<>();
        this.extraHooks = new HashMap<>();
    }

    public Map<String, Hook> getHooks() {
        return this.hooks;
    }

    @Override
    public void onEnable() {
        getDataFolder().mkdirs();

        getConfig().options().copyDefaults(true);

        getConfig().options().header("Plan Config\n"
                + "debug - Errors are saved in errorlog.txt when they occur\n"
                + "visible - Plugin's data is accessable with /plan inspect command"
        );

        saveConfig();

        try {
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                String[] placeholders = DataUtils.getPlaceholdersFileData();
                if (placeholders != null) {
                    this.placeholderAPIHook = new PlaceholderAPIHook(this, placeholders);
                    PlaceholderAPIHook phAHook = (PlaceholderAPIHook) placeholderAPIHook;
                    phAHook.hook();
                } else {
                    logToFile("Failed to read placeholders.yml\n");
                }
            }
        } catch (Exception e) {
            logError("Failed to create placeholders.yml");
            logToFile("Failed to create placeholders.yml\n" + e);
        }

        List<String> hookFail = hookInit();
        if (this.hooks.isEmpty()) {
            logError("Found no plugins to get data (or config set to false). Disabling plugin..");
            logToFile("MAIN\nNo Hooks found. Plugin Disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.api = new API(this);

        String loadedMsg = "Hooked into: ";
        for (String key : this.hooks.keySet()) {
            loadedMsg += ChatColor.GREEN + key + " ";
        }
        String failedMsg = "Not Hooked: ";
        for (String string : hookFail) {
            failedMsg += ChatColor.RED + string + " ";
        }
        Bukkit.getServer().getConsoleSender().sendMessage("[Plan] " + loadedMsg);
        if (!hookFail.isEmpty()) {
            Bukkit.getServer().getConsoleSender().sendMessage("[Plan] " + failedMsg);
        }

        getCommand("plan").setExecutor(new PlanCommand(this));

        log("Player Analytics Enabled.");
    }

    public List<String> hookInit() {
        this.hooks.clear();
        List<String> hookFail = new ArrayList<>();
        String[] plugins = {"OnTime", "Essentials", "Towny", "Vault", "Factions", "SuperbVote", "AdvancedAchievements"};
        for (String pluginName : plugins) {

            if (getConfig().getBoolean("visible." + pluginName.toLowerCase())) {
                try {
                    String className = "com.djrapitops.plan.command.hooks." + pluginName + "Hook";
                    Class<Hook> clazz = (Class<Hook>) Hook.class.forName(className);
                    this.hooks.put(pluginName, clazz.getConstructor(Plan.class).newInstance(this));
                } catch (Exception | NoClassDefFoundError e) {
                    hookFail.add(pluginName);
                    String toLog = "MAIN-HOOKINIT\nFailed to hook " + pluginName + "\n" + e;
                    toLog += "\n" + e.getCause();
                    logToFile(toLog);

                }
            } else {
                hookFail.add(ChatColor.YELLOW + pluginName);
            }
        }
        for (String extraHook : this.extraHooks.keySet()) {
            this.hooks.put(extraHook, this.extraHooks.get(extraHook));
        }
        if (getConfig().getBoolean("visible.placeholderapi")) {
            if (this.placeholderAPIHook != null) {
                this.hooks.put("PlaceholderAPI", this.placeholderAPIHook);
            } else {
                hookFail.add("PlaceholderAPI");
            }
        } else {
            hookFail.add(ChatColor.YELLOW + "PlaceholderAPI");
        }
        return hookFail;
    }

    @Override
    public void onDisable() {
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
            File log = new File(getDataFolder(), "errorlog.txt");
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
                logError("Failed to create log.txt file");
            }
        }
    }

    public Hook getPlaceholderAPIHook() {
        return this.placeholderAPIHook;
    }

    public API getAPI() {
        return api;
    }
    
    public void addExtraHook(String name, Hook hook) {
        this.extraHooks.put(name, hook);
        this.hooks.put(name, hook);
    }
}
