package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsBukkitAPI;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.API;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsHook extends Hook {

    private AdvancedAchievements hookedPlugin;
    private AdvancedAchievementsAPI aaAPI;

    /**
     * Hooks the plugin and calculates Total Achievements.
     */
    public AdvancedAchievementsHook() throws NoClassDefFoundError {
        super("com.hm.achievement.AdvancedAchievements");
        if (enabled) {
            if (Integer.parseInt(Character.toString(hookedPlugin.getDescription().getVersion().charAt(0))) >= 5) {
                aaAPI = AdvancedAchievementsBukkitAPI.linkAdvancedAchievements();
                API planAPI = Plan.getPlanAPI();
                planAPI.addPluginDataSource(new AdvancedAchievementsTable(aaAPI));
                planAPI.addPluginDataSource(new AdvancedAchievementsTotalAchievements(calcTotalAchievements()));
                planAPI.addPluginDataSource(new AdvancedAchievementsAchievements(aaAPI));
            } else {
                enabled = false;
            }
        }
    }

    private int calcTotalAchievements() {
        int total = 0;
        for (NormalAchievements category : NormalAchievements.values()) {
            String categoryName = category.toString();
            if (hookedPlugin.getDisabledCategorySet().contains(categoryName)) {
                // Ignore this type.
                continue;
            }
            total += hookedPlugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size();
        }
        for (MultipleAchievements category : MultipleAchievements.values()) {
            String categoryName = category.toString();
            if (hookedPlugin.getDisabledCategorySet().contains(categoryName)) {
                // Ignore this type.
                continue;
            }
            for (String item : hookedPlugin.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
                total += hookedPlugin.getPluginConfig().getConfigurationSection(categoryName + '.' + item)
                        .getKeys(false).size();
            }
        }
        if (!hookedPlugin.getDisabledCategorySet().contains("Commands")) {
            total += hookedPlugin.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size();
        }
        return total;
    }
}
