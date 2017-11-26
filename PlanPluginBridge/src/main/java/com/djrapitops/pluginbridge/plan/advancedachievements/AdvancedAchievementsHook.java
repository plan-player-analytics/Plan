package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.djrapitops.pluginbridge.plan.Hook;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsBukkitAPI;
import main.java.com.djrapitops.plan.api.API;
import main.java.com.djrapitops.plan.data.additional.HookHandler;

import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 * A Class responsible for hooking to AdvancedAchievements and registering 2
 * data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
public class AdvancedAchievementsHook extends Hook {

    /**
     * Hooks the plugin and registers it's PluginData objects.
     * <p>
     * API#addPluginDataSource uses the same method from HookHandler.
     *
     * @param hookH HookHandler instance for registering the data sources.
     * @throws NoClassDefFoundError when the plugin class can not be found.
     * @see API
     */
    public AdvancedAchievementsHook(HookHandler hookH) {
        super("com.hm.achievement.AdvancedAchievements", hookH);
    }

    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            AdvancedAchievements aa = getPlugin(AdvancedAchievements.class);
            if (Integer.parseInt(Character.toString(aa.getDescription().getVersion().charAt(0))) >= 5) {
                AdvancedAchievementsAPI aaAPI = AdvancedAchievementsBukkitAPI.linkAdvancedAchievements();
                addPluginDataSource(new AdvancedAchievementsData(aaAPI));
            } else {
                enabled = false;
            }
        }
    }
}
