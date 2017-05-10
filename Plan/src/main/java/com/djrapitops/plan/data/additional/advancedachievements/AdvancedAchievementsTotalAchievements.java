package main.java.com.djrapitops.plan.data.additional.advancedachievements;

import java.io.Serializable;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.additional.AnalysisType;
import main.java.com.djrapitops.plan.data.additional.PluginData;

/**
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsTotalAchievements extends PluginData {

    private int totalAchievements;

    public AdvancedAchievementsTotalAchievements(int totalAchievements) {
        super("AdvancedAchievements", "totalachievements", AnalysisType.TOTAL_VALUE);
        super.setAnalysisOnly(false);
        super.setPrefix("Total Achivements: ");
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        return totalAchievements + "";
    }

    @Override
    public Serializable getValue(UUID uuid) {
        return totalAchievements;
    }

}
