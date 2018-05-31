/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.analysis.MathUtils;
import com.djrapitops.plugin.api.TimeAmount;
import com.hm.achievement.api.AdvancedAchievementsAPI;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * PluginData class for AdvancedAchievements.
 *
 * @author Rsl1122
 */
public class AdvancedAchievementsData extends PluginData {

    private final AdvancedAchievementsAPI aaAPI;
    private long lastRefresh;
    private Map<UUID, Integer> totalAchievements;

    public AdvancedAchievementsData(AdvancedAchievementsAPI aaAPI) {
        super(ContainerSize.THIRD, "AdvancedAchievements");
        super.setPluginIcon("star");
        super.setIconColor("green");
        this.aaAPI = aaAPI;
        refreshTotalAchievements();
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String text = getWithIcon("Achievements", "check-circle-o", "green");
        inspectContainer.addValue(text, aaAPI.getPlayerTotalAchievements(uuid));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        if (System.currentTimeMillis() - lastRefresh > TimeAmount.MINUTE.ms() * 5L) {
            refreshTotalAchievements();
        }
        long total = getTotal(totalAchievements);
        String average = FormatUtils.cutDecimals(MathUtils.averageDouble(total, totalAchievements.size()));

        analysisContainer.addValue(getWithIcon("Total Achievements", "check-circle-o", "green"), total);
        analysisContainer.addValue(getWithIcon("Average Achievements", "check-circle-o", "green"), average);
        analysisContainer.addPlayerTableValues(getWithIcon("Achievements", "star"), totalAchievements);
        return analysisContainer;
    }

    private long getTotal(Map<UUID, Integer> totalAchievements) {
        return MathUtils.sumLong(totalAchievements.values().stream().map(i -> (long) i));
    }

    private void refreshTotalAchievements() {
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = System.currentTimeMillis();
    }
}