package com.djrapitops.plan;

import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.api.API;
import com.djrapitops.planlite.api.DataPoint;
import java.util.HashMap;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
        if (plugin.getConfig().getBoolean("Settings.PlanLite.Enabled")) {
            if (Bukkit.getPluginManager().isPluginEnabled("PlanLite")) {
                try {
                    this.planLite = getPlugin(PlanLite.class);
                    if (planLite == null) {
                        throw new Exception(Phrase.ERROR_PLANLITE.toString());
                    }
                    enabled = true;
                    planLiteApi = planLite.getAPI();
                } catch (Exception e) {
                }
            } else {
                enabled = false;
            }
        } else {
            enabled = false;
        }
    }

    public Set<String> getEnabledHooksNames() {
        return planLite.getHooks().keySet();
    }

    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) {
        return planLiteApi.getData(playerName, dataPoint);
    }

    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) {
        return planLiteApi.getAllData(playerName, dataPoint);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean passCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        return planLite.getPlanCommand().onCommand(sender, cmd, commandLabel, args);
    }
}
