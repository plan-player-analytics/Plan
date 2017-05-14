package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

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
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return parseContainer(modifierPrefix, aaAPI.getPlayerTotalAchievements(uuid) + "");
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return aaAPI.getPlayerTotalAchievements(uuid);
    }

}
