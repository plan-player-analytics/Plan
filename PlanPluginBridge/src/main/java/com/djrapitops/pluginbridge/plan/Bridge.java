package com.djrapitops.pluginbridge.plan;

import main.java.com.djrapitops.plan.data.additional.HookHandler;

/**
 * @author Rsl1122
 */
@SuppressWarnings("WeakerAccess")
public class Bridge {

    private Bridge() {
        throw new IllegalStateException("Utility class");
    }

    public static void hook(HookHandler handler) {
        String[] plugins = new String[]{
                "AdvancedAchievements", "Essentials", "Factions", "Mcmmo",
                "Jobs", "OnTime", "Towny", "Valut", "ASkyBlock",
                "GriefPrevention", "LiteBans", "SuperbVote", "ViaVersion"
        };
        for (String pluginName : plugins) {
            try {
                String className = "com.djrapitops.pluginbridge.plan." + pluginName + "Hook";
                Class<Hook> clazz = (Class<Hook>) Hook.class.forName(className);
                clazz.getConstructor(HookHandler.class).newInstance(handler);
            } catch (Exception | NoClassDefFoundError ignore) {
            }
        }
    }
}
