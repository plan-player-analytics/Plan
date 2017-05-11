package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import main.java.com.djrapitops.plan.data.additional.Hook;
import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.api.AdvancedAchievementsAPI;
import com.hm.achievement.api.AdvancedAchievementsBukkitAPI;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsHook extends Hook {

    private AdvancedAchievements aa;

    /**
     * Hooks the plugin and calculates Total Achievements.
     */
    public AdvancedAchievementsHook(HookHandler hookH) throws NoClassDefFoundError {
        super("com.hm.achievement.AdvancedAchievements");
        if (enabled) {
            aa = getPlugin(AdvancedAchievements.class);
            if (Integer.parseInt(Character.toString(aa.getDescription().getVersion().charAt(0))) >= 5) {
                AdvancedAchievementsAPI aaAPI = AdvancedAchievementsBukkitAPI.linkAdvancedAchievements();
                hookH.addPluginDataSource(new AdvancedAchievementsAchievements(aaAPI));
                hookH.addPluginDataSource(new AdvancedAchievementsTable(aaAPI));
            } else {
                enabled = false;
            }
        }
    }
}
