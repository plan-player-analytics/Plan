package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.pluginbridge.plan.Hook;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsAPIFetcher;

import java.util.Optional;

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
     */
    public AdvancedAchievementsHook(HookHandler hookH) {
        super("com.hm.achievement.AdvancedAchievements", hookH);
    }

    @Override
    public void hook() throws NoClassDefFoundError {
        if (enabled) {
            Optional<AdvancedAchievementsAPI> aaAPI = AdvancedAchievementsAPIFetcher.fetchInstance();
            if (aaAPI.isPresent()) {
                addPluginDataSource(new AdvancedAchievementsData(aaAPI.get()));
            } else {
                enabled = false;
            }
        }
    }
}
