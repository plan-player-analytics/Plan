package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import com.hm.achievement.api.AdvancedAchievementsAPI;
import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsAchievements extends PluginData {

    private AdvancedAchievementsAPI aaAPI;

    public AdvancedAchievementsAchievements(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievements", new AnalysisType[]{AnalysisType.INT_TOTAL, AnalysisType.INT_AVG});
        super.setAnalysisOnly(false);
        super.setPrefix("Achivements: ");
        super.setSuffix(" / %totalachievements%");
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
