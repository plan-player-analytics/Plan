package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.pluginbridge.plan.Hook;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsAPIFetcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * A Class responsible for hooking to AdvancedAchievements and registering 2
 * data sources.
 *
 * @author Rsl1122
 * @since 3.1.0
 */
@Singleton
public class AdvancedAchievementsHook extends Hook {

    private final Formatter<Double> decimalFormatter;

    @Inject
    public AdvancedAchievementsHook(
            Formatters formatters
    ) {
        super("com.hm.achievement.AdvancedAchievements");
        decimalFormatter = formatters.decimals();
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
            Optional<AdvancedAchievementsAPI> aaAPI = AdvancedAchievementsAPIFetcher.fetchInstance();
            if (aaAPI.isPresent()) {
                handler.addPluginDataSource(new AdvancedAchievementsData(aaAPI.get(), decimalFormatter));
            } else {
                enabled = false;
            }
        }
    }
}
