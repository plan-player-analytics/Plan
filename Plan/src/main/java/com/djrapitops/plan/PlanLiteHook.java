package com.djrapitops.plan;

import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.api.API;
import com.djrapitops.planlite.api.DataPoint;
import java.util.HashMap;
import java.util.Set;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.handlers.PlanLiteDataPushHook;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class PlanLiteHook {

    private PlanLite planLite;
    private Plan plugin;
    private API planLiteApi;

    private boolean enabled;

    /**
     * Class Constructor.
     *
     * Attempts to hook to PlanLite, if not present sets enabled to false
     *
     * @param plugin
     */
    public PlanLiteHook(Plan plugin) {
        this.plugin = plugin;
        if (Settings.PLANLITE_ENABLED.isTrue()) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlanLite")) {
                try {
                    this.planLite = getPlugin(PlanLite.class);
                    if (planLite == null) {
                        throw new Exception(Phrase.ERROR_PLANLITE.toString());
                    }
                    enabled = true;
                    planLiteApi = planLite.getAPI();
                    if (Settings.USE_ALTERNATIVE_UI.isTrue()) {
                        planLite.addExtraHook("Plan", new PlanLiteDataPushHook(plugin));
                    }
                } catch (Exception e) {
                }
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    /**
     * @return Set of the Enabled hooks names.
     */
    public Set<String> getEnabledHooksNames() {
        return planLite.getHooks().keySet();
    }

    /**
     * Used to get data from PlanLite for the Analysis & Inspect.
     *
     * @param playerName Name of the player.
     * @param dataPoint true (use datapoint system)
     * @return The data about the player.
     */
    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) {
        return planLiteApi.getData(playerName, dataPoint);
    }

    /**
     * Used to get data from PlanLite for the Analysis & Inspect.
     *
     * @param playerName Name of the player.
     * @param dataPoint true (use datapoint system)
     * @return The data about the player.
     */
    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) {
        return planLiteApi.getAllData(playerName, dataPoint);
    }

    /**
     * @return true if the config setting is true & PlanLite is installed.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Passes commands to PlanLite.
     * 
     * Used by /plan lite
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return
     */
    public boolean passCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return planLite.getPlanCommand().onCommand(sender, cmd, commandLabel, args);
    }

    /**
     * @return true if server has towny
     */
    public boolean hasTowny() {
        return getEnabledHooksNames().contains("Towny");
    }

    /**
     * @return true if server has factions
     */
    public boolean hasFactions() {
        return getEnabledHooksNames().contains("Factions");
    }

    /**
     * @return true if server has superbvote
     */
    public boolean hasSuperbVote() {
        return getEnabledHooksNames().contains("SuperbVote");
    }

    /**
     * @return true if server has vault
     */
    public boolean hasVault() {
        return getEnabledHooksNames().contains("Vault");
    }
}
