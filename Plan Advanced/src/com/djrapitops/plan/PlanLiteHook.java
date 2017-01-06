package com.djrapitops.plan;

import com.djrapitops.plan.api.API;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.Hook;
import java.util.HashMap;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class PlanLiteHook {

    private PlanLite planLite;
    private Plan plugin;
    private API planLiteApi;

    public PlanLiteHook(Plan plugin) {
        try {
            this.planLite = getPlugin(PlanLite.class);
            if (planLite == null) {
                throw new Exception(Phrase.ERROR_PLANLITE.toString());
            }
            planLiteApi = planLite.getAPI();
        } catch (Exception e) {
            plugin.logError(e.toString());
        }
    }

    void addExtraHook(String name, Hook hook) {
        try {
            if (planLite == null) {
                throw new Exception(Phrase.ERROR_PLANLITE.toString());
            }
            planLite.addExtraHook(name, hook);
            plugin.log(Phrase.PLANLITE_REG_HOOK.toString() + name);
        } catch (Exception | NoClassDefFoundError e) {
            plugin.logError("Failed to hook " + name + "\n  " + e);
        }
    }

    @Deprecated
    public boolean getDebug() {
        return planLiteApi.getDebug();
    }

    @Deprecated
    public boolean getVisibleEssentials() {
        return planLiteApi.getVisibleEssentials();
    }

    @Deprecated
    public boolean getVisibleOnTime() {
        return planLiteApi.getVisibleOnTime();
    }

    @Deprecated
    public boolean getVisibleFactions() {
        return planLiteApi.getVisibleFactions();
    }

    @Deprecated
    public boolean getVisibleSuperbVote() {
        return planLiteApi.getVisibleSuperbVote();
    }

    @Deprecated
    public boolean getVisibleTowny() {
        return planLiteApi.getVisibleTowny();
    }

    @Deprecated
    public boolean getVisibleVault() {
        return planLiteApi.getVisibleVault();
    }

    @Deprecated
    public boolean getVisibleAdvancedAchievements() {
        return planLiteApi.getVisibleAdvancedAchievements();
    }

    @Deprecated
    public boolean getVisiblePlaceholderAPI() {
        return planLiteApi.getVisiblePlaceholderAPI();
    }

    @Deprecated
    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) {
        return planLiteApi.getData(playerName, dataPoint);
    }

    @Deprecated
    public HashMap<String, String> getData(String playerName) {
        return planLiteApi.getData(playerName);
    }

    @Deprecated
    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) {
        return planLiteApi.getAllData(playerName, dataPoint);
    }

    @Deprecated
    public HashMap<String, String> getAllData(String playerName) {
        return planLiteApi.getAllData(playerName);
    }

    @Deprecated
    public HashMap<String, DataPoint> transformOldDataFormat(HashMap<String, String> oldData) {
        return planLiteApi.transformOldDataFormat(oldData);
    }
}
