package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.Date;
import java.util.HashMap;

public class API {

    private Plan plugin;

    public API(Plan plugin) {
        this.plugin = plugin;
    }

    public boolean getDebug() {
        return plugin.getConfig().getBoolean("debug");
    }

    public boolean getVisibleEssentials() {
        return plugin.getConfig().getBoolean("visible.essentials");
    }

    public boolean getVisibleOnTime() {
        return plugin.getConfig().getBoolean("visible.ontime");
    }

    public boolean getVisibleFactions() {
        return plugin.getConfig().getBoolean("visible.factions");
    }

    public boolean getVisibleSuperbVote() {
        return plugin.getConfig().getBoolean("visible.superbvote");
    }

    public boolean getVisibleTowny() {
        return plugin.getConfig().getBoolean("visible.towny");
    }

    public boolean getVisibleVault() {
        return plugin.getConfig().getBoolean("visible.vault");
    }

    public boolean getVisibleAdvancedAchievements() {
        return plugin.getConfig().getBoolean("visible.advancedachievements");
    }

    public boolean getVisiblePlaceholderAPI() {
        return plugin.getConfig().getBoolean("visible.placeholderapi");
    }
    
    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) {
        return DataFormatUtils.removeExtraDataPoints(DataUtils.getData(false, playerName));
    }
    
    // Please move to DataPoint system as soon as possible
    @Deprecated
    public HashMap<String, String> getData(String playerName) {
        HashMap<String, String> data = new HashMap<>();
        HashMap<String, DataPoint> dataWPoints = getData(playerName, true);
        dataWPoints.keySet().parallelStream().forEach((key) -> {
            data.put(key, dataWPoints.get(key).data());
        });
        return data;
    }
    
    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) {
        return DataFormatUtils.removeExtraDataPoints(DataUtils.getData(true, playerName));
    }
    
    // Please move to DataPoint system as soon as possible
    @Deprecated
    public HashMap<String, String> getAllData(String playerName) {
        HashMap<String, String> data = new HashMap<>();
        HashMap<String, DataPoint> dataWPoints = getAllData(playerName, true);
        dataWPoints.keySet().parallelStream().forEach((key) -> {
            data.put(key, dataWPoints.get(key).data());
        });
        return data;
    }
    
    public HashMap<String, DataPoint> transformOldDataFormat(HashMap<String, String> oldData) {
        HashMap<String, DataPoint> data = new HashMap<>();
        for (String key : oldData.keySet()) {
            data.put(key, new DataPoint(oldData.get(key), DataType.OTHER));
        }
        return data;
    }
    
    // use (new Date) on after parameter for time since moment to now
    public static String formatTimeSinceDate(Date before, Date after) {
        return DataFormatUtils.formatTimeAmountSinceDate(before, after);
    }
    
    // use (new Date) on after parameter for time since moment to now
    public static String formatTimeSinceString(String before, Date after) {
        return DataFormatUtils.formatTimeAmountSinceString(before, after);
    }
    
    public static String formatTimeAmount(String timeInMs) {
        return DataFormatUtils.formatTimeAmount(timeInMs);
    }
    
    public static String formatTimeStamp(String timeInMs) {
        return DataFormatUtils.formatTimeStamp(timeInMs);
    }
    
    public void addExtraHook(String name, Hook hook) {
        plugin.addExtraHook(name, hook);
    }
}
