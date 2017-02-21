package main.java.com.djrapitops.plan.data.additional;

import com.hm.achievement.AdvancedAchievements;
import com.hm.achievement.category.MultipleAchievements;
import com.hm.achievement.category.NormalAchievements;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsHook extends Hook {

    private final Plan plugin;
    private AdvancedAchievements adAc;
    private int totalAchievements;

    /**
     * Hooks the plugin and calculates Total Achievements.
     *
     * @param plugin
     */
    public AdvancedAchievementsHook(Plan plugin) throws NoClassDefFoundError {
        super("com.hm.achievement.AdvancedAchievements");
        this.plugin = plugin;
        if (super.isEnabled()) {
            try {
                totalAchievements = calcTotalAchievements();
            } catch (Exception | NoClassDefFoundError e) {
                super.setEnabled(false);
            }
        }
    }

    private int calcTotalAchievements() throws Exception, NoClassDefFoundError {
        int total = 0;
        for (NormalAchievements category : NormalAchievements.values()) {
            String categoryName = category.toString();
            if (adAc.getDisabledCategorySet().contains(categoryName)) {
                // Ignore this type.
                continue;
            }
            total += adAc.getPluginConfig().getConfigurationSection(categoryName).getKeys(false).size();
        }
        for (MultipleAchievements category : MultipleAchievements.values()) {
            String categoryName = category.toString();
            if (adAc.getDisabledCategorySet().contains(categoryName)) {
                // Ignore this type.
                continue;
            }
            for (String item : adAc.getPluginConfig().getConfigurationSection(categoryName).getKeys(false)) {
                total += adAc.getPluginConfig().getConfigurationSection(categoryName + '.' + item)
                        .getKeys(false).size();
            }
        }
        if (!adAc.getDisabledCategorySet().contains("Commands")) {
            total += adAc.getPluginConfig().getConfigurationSection("Commands").getKeys(false).size();
        }
        return total;
    }

    /**
     * Returns total number of achievements. isEnabled() should be called before
     * calling this method
     *
     * @return Total Achievements calculated during Initialization
     */
    public int getTotalAchievements() {
        return totalAchievements;
    }

    /**
     * Returns achievement number of a player. isEnabled() should be called
     * before calling this method
     *
     * @param uuid UUID of player
     * @return Achievement amount of the Player
     */
    public int getPlayerAchievements(UUID uuid) {
        return adAc.getDb().getPlayerAchievementsAmount(uuid.toString());
    }
}
