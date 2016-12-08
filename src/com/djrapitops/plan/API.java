package com.djrapitops.plan;

import com.djrapitops.plan.command.hooks.Hook;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
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
    
    public HashMap<String, String> getData(String playerName) {
        return DataFormatUtils.removeExtraDataPoints(DataUtils.getData(false, playerName));
    }
    
    public HashMap<String, String> getAllData(String playerName) {
        return DataFormatUtils.removeExtraDataPoints(DataUtils.getData(true, playerName));
    }
    
    public void addExtraHook(String name, Hook hook) {
        plugin.addExtraHook(name, hook);
    }
}
