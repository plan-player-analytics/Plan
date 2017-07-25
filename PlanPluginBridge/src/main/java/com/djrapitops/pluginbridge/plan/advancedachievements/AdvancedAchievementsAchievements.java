package com.djrapitops.pluginbridge.plan.advancedachievements;

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

    private final AdvancedAchievementsAPI aaAPI;
    private long lastRefresh;
    private Map<UUID, Integer> totalAchievements;

    /**
     * Class Constructor, sets the parameters of the PluginData object.
     *
     * @param aaAPI AdvancedAchievementsAPI given by AdvancedAchievementsHook
     */
    public AdvancedAchievementsAchievements(AdvancedAchievementsAPI aaAPI) {
        super("AdvancedAchievements", "achievements", AnalysisType.INT_TOTAL, AnalysisType.INT_AVG);
        this.aaAPI = aaAPI;
        super.setAnalysisOnly(false);
        super.setIcon("check-circle-o");
        super.setPrefix("Achievements: ");
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = MiscUtils.getTime();
    }

    @Override
    public String getHtmlReplaceValue(String modifierPrefix, UUID uuid) {
        if (MiscUtils.getTime() - lastRefresh > 60000) {
            refreshTotalAchievements();
        }
        Integer total = totalAchievements.get(uuid);
        if (total != null) {
            return parseContainer(modifierPrefix, total + "");
        }
        return parseContainer(modifierPrefix, 0 + "");
    }

    private void refreshTotalAchievements() {
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = MiscUtils.getTime();
    }

    @Override
    public Serializable getValue(UUID uuid) {
        if (MiscUtils.getTime() - lastRefresh > 60000) {
            refreshTotalAchievements();
        }
        Integer total = totalAchievements.get(uuid);
        if (total != null) {
            return total;
        }
        return -1;
    }

}
