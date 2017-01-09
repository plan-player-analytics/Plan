package com.djrapitops.planlite;

import com.djrapitops.planlite.api.API;
import com.djrapitops.planlite.command.hooks.EssentialsHook;
import com.djrapitops.planlite.command.hooks.FactionsHook;
import com.djrapitops.planlite.command.hooks.OnTimeHook;
import com.djrapitops.planlite.api.Hook;
import com.djrapitops.planlite.command.hooks.SuperbVoteHook;
import com.djrapitops.planlite.command.hooks.TownyHook;
import com.djrapitops.planlite.command.hooks.VaultHook;
import com.djrapitops.planlite.command.hooks.AdvancedAchievementsHook;
import com.djrapitops.planlite.command.hooks.BukkitDataHook;
import com.djrapitops.planlite.command.hooks.PlayerLoggerHook;
import com.djrapitops.planlite.command.utils.DataUtils;
import com.djrapitops.planlite.command.utils.MiscUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class PlanLite extends JavaPlugin {

    private final Map<String, Hook> hooks;
    private API api;
    private final Map<String, Hook> extraHooks;

    public PlanLite() {
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

        getConfig().options().header("PlanLite Config\n"
                + "debug - Errors are saved in errorlog.txt when they occur\n"
                + "visible - Plugin's data is accessable with /plan inspect command"
        );

        saveConfig();
        List<String> hookFail = hookInit();
        if (this.hooks.isEmpty()) {
            logError("Found no plugins to get data (or config set to false). Disabling plugin..");
            logToFile("MAIN\nNo Hooks found. Plugin Disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.api = new API(this);

        log(MiscUtils.checkVersion());

        String loadedMsg = "Hooked into: ";
        for (String key : this.hooks.keySet()) {
            loadedMsg += ChatColor.GREEN + key + " ";
        }
        String failedMsg = "Not Hooked: ";
        for (String string : hookFail) {
            failedMsg += ChatColor.RED + string + " ";
        }
        Bukkit.getServer().getConsoleSender().sendMessage("[PlanLite] " + loadedMsg);
        if (!hookFail.isEmpty()) {
            Bukkit.getServer().getConsoleSender().sendMessage("[PlanLite] " + failedMsg);
        }

        getCommand("plan").setExecutor(new PlanCommand(this));

        log("Player Analytics Enabled.");
    }

    public List<String> hookInit() {
        this.hooks.clear();
        List<String> hookFail = new ArrayList<>();
        String[] pluginsArray = {"OnTime", "Essentials", "Towny", "Vault", "Factions", "SuperbVote",
            "AdvancedAchievements", "BukkitData", "PlayerLogger"};
        List<String> plugins = new ArrayList<>();
        plugins.addAll(Arrays.asList(pluginsArray));
        StringBuilder errors = new StringBuilder();
        errors.append("MAIN-HOOKINIT\n");
        plugins.parallelStream().forEach((pluginName) -> {
            if (getConfig().getBoolean("visible." + pluginName.toLowerCase())) {
                try {
                    String className = "com.djrapitops.planlite.command.hooks." + pluginName + "Hook";
                    Class<Hook> clazz = (Class<Hook>) Hook.class.forName(className);
                    this.hooks.put(pluginName, clazz.getConstructor(PlanLite.class).newInstance(this));
                } catch (Exception | NoClassDefFoundError e) {
                    hookFail.add(pluginName);
                    errors.append("Failed to hook ").append(pluginName).append("\n").append(e);
                    errors.append("\n").append(e.getCause());
                }
            } else {
                hookFail.add(ChatColor.YELLOW + pluginName);
            }
        });
        if (!errors.toString().equals("MAIN-HOOKINIT\n")) {
            logToFile(errors.toString());
        }
        for (String extraHook : this.extraHooks.keySet()) {
            this.hooks.put(extraHook, this.extraHooks.get(extraHook));
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

    public API getAPI() {
        return api;
    }

    /**
     *
     * @param name
     * @param hook
     */
    public void addExtraHook(String name, Hook hook) {
        try {
            this.extraHooks.put(name, hook);
            this.hooks.put(name, hook);
            log("Registered additional hook: " + name);
        } catch (Exception | NoClassDefFoundError e) {
            logToFile("Failed to hook " + name + "\n" + e);
        }
    }
}
