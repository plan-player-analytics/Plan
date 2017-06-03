package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * PluginData class for AdvancedAchievements-plugin.
 *
 * Registered to the plugin by AdvancedAchievementsHook.
 *
 * Gives the amount of achievements as value.
 *
 * @author Rsl1122
 * @since 3.1.0
 * @see AdvancedAchievementsHook
 */
public class AdvancedAchievementsAchievements extends PluginData {

    private AdvancedAchievementsAPI aaAPI;
    private long lastRefresh;
    private Map<UUID, Integer> totalAchievements;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     */
    public AdvancedAchievementsAchievements(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievements", new AnalysisType[]{AnalysisType.INT_TOTAL, AnalysisType.INT_AVG});
        this.aaAPI = aaAPI;
        super.setAnalysisOnly(false);
        super.setIcon("check-circle-o");
        super.setPrefix("Achivements: ");
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = MiscUtils.getTime();
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        if (MiscUtils.getTime()- lastRefresh > 60000) {
            totalAchievements = aaAPI.getPlayersTotalAchievements();
        }
        if (totalAchievements.containsKey(uuid)) {
            return parseContainer(modifierPrefix,totalAchievements.get(uuid) + "");
        }
        return parseContainer(modifierPrefix, 0+"");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        if (MiscUtils.getTime()- lastRefresh > 60000) {
            totalAchievements = aaAPI.getPlayersTotalAchievements();
        }
        if (totalAchievements.containsKey(uuid)) {
            return totalAchievements.get(uuid);
        }
        return -1;
    }

}
