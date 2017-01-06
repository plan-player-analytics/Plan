package com.djrapitops.plan.api;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanLiteHook;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import java.util.Date;
import java.util.HashMap;

public class API {

    private Plan plugin;
    private PlanLiteHook hook;

    public API(Plan plugin) {
        this.plugin = plugin;
        hook = plugin.getPlanLiteHook();
    }

    public static String formatTimeSinceDate(Date before, Date after) {
        return DataFormatUtils.formatTimeAmountSinceDate(before, after);
    }

    public static String formatTimeSinceString(String before, Date after) {
        return DataFormatUtils.formatTimeAmountSinceString(before, after);
    }

    public static String formatTimeAmount(String timeInMs) {
        return DataFormatUtils.formatTimeAmount(timeInMs);
    }

    public static String formatTimeStamp(String timeInMs) {
        return DataFormatUtils.formatTimeStamp(timeInMs);
    }

    /*
        Deprecated this part of the API, move to PlanLite API to register hooks
        If PlanLite is installed PlanLiteAPI methods will be attempted
        If PlanLite is not installed -> NullPointerException will be thrown.
     */
    @Deprecated
    public boolean getDebug() throws NullPointerException {
        return hook.getDebug();
    }

    @Deprecated
    public boolean getVisibleEssentials() throws NullPointerException {
        return hook.getVisibleEssentials();
    }

    @Deprecated
    public boolean getVisibleOnTime() throws NullPointerException {
        return hook.getVisibleOnTime();
    }

    @Deprecated
    public boolean getVisibleFactions() throws NullPointerException {
        return hook.getVisibleFactions();
    }

    @Deprecated
    public boolean getVisibleSuperbVote() throws NullPointerException {
        return hook.getVisibleSuperbVote();
    }

    @Deprecated
    public boolean getVisibleTowny() throws NullPointerException {
        return hook.getVisibleTowny();
    }

    @Deprecated
    public boolean getVisibleVault() throws NullPointerException {
        return hook.getVisibleVault();
    }

    @Deprecated
    public boolean getVisibleAdvancedAchievements() throws NullPointerException {
        return hook.getVisibleAdvancedAchievements();
    }

    @Deprecated
    public boolean getVisiblePlaceholderAPI() throws NullPointerException {
        return hook.getVisiblePlaceholderAPI();
    }

    @Deprecated
    public HashMap<String, DataPoint> getData(String playerName, boolean dataPoint) throws NullPointerException {
        return hook.getData(playerName, dataPoint);
    }

    @Deprecated
    public HashMap<String, String> getData(String playerName) throws NullPointerException {
        return hook.getData(playerName);
    }

    @Deprecated
    public HashMap<String, DataPoint> getAllData(String playerName, boolean dataPoint) throws NullPointerException {
        return hook.getAllData(playerName, dataPoint);
    }

    @Deprecated
    public HashMap<String, String> getAllData(String playerName) throws NullPointerException {
        return hook.getAllData(playerName);
    }

    @Deprecated
    public HashMap<String, DataPoint> transformOldDataFormat(HashMap<String, String> oldData) throws NullPointerException {
        return hook.transformOldDataFormat(oldData);
    }

    @Deprecated
    public void addExtraHook(String name, Hook hook) throws NullPointerException {
        plugin.addExtraHook(name, hook);
    }
}
