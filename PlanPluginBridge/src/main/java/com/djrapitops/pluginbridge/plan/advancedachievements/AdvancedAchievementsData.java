/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.pluginbridge.plan.advancedachievements;

import com.djrapitops.plan.data.element.AnalysisContainer;
import com.djrapitops.plan.data.element.InspectContainer;
import com.djrapitops.plan.data.plugin.ContainerSize;
import com.djrapitops.plan.data.plugin.PluginData;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.html.icon.Color;
import com.djrapitops.plan.utilities.html.icon.Family;
import com.djrapitops.plan.utilities.html.icon.Icon;
import com.hm.achievement.api.AdvancedAchievementsAPI;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * PluginData class for AdvancedAchievements.
 *
 * @author Rsl1122
 */
class AdvancedAchievementsData extends PluginData {

    private final AdvancedAchievementsAPI aaAPI;
    private final Formatter<Double> decimalFormatter;

    private long lastRefresh;
    private Map<UUID, Integer> totalAchievements;

    AdvancedAchievementsData(
            AdvancedAchievementsAPI aaAPI,
            Formatter<Double> decimalFormatter
    ) {
        super(ContainerSize.THIRD, "AdvancedAchievements");
        this.decimalFormatter = decimalFormatter;
        setPluginIcon(Icon.called("star").of(Color.GREEN).build());
        this.aaAPI = aaAPI;
        refreshTotalAchievements();
    }

    @Override
    public InspectContainer getPlayerData(UUID uuid, InspectContainer inspectContainer) {
        String text = getWithIcon("Achievements", Icon.called("check-circle").of(Family.REGULAR).of(Color.GREEN));
        inspectContainer.addValue(text, aaAPI.getPlayerTotalAchievements(uuid));

        return inspectContainer;
    }

    @Override
    public AnalysisContainer getServerData(Collection<UUID> collection, AnalysisContainer analysisContainer) {
        if (System.currentTimeMillis() - lastRefresh > TimeUnit.MINUTES.toMillis(5L)) {
            refreshTotalAchievements();
        }
        long total = getTotal(totalAchievements);
        int size = totalAchievements.size();
        String average = size != 0 ? decimalFormatter.apply(total * 1.0 / size) : "-";

        analysisContainer.addValue(getWithIcon("Total Achievements", Icon.called("check-circle").of(Family.REGULAR).of(Color.GREEN)), total);
        analysisContainer.addValue(getWithIcon("Average Achievements", Icon.called("check-circle").of(Family.REGULAR).of(Color.GREEN)), average);
        analysisContainer.addPlayerTableValues(getWithIcon("Achievements", Icon.called("star")), totalAchievements);
        return analysisContainer;
    }

    private long getTotal(Map<UUID, Integer> totalAchievements) {
        return totalAchievements.values().stream().mapToInt(i -> i).sum();
    }

    private void refreshTotalAchievements() {
        totalAchievements = aaAPI.getPlayersTotalAchievements();
        lastRefresh = System.currentTimeMillis();
    }
}