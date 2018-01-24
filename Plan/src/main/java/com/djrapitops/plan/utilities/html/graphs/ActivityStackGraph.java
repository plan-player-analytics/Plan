/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.element.ActivityIndex;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Stack Graph that represents evolution of the PlayerBase in terms of ActivityIndex Groups.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
public class ActivityStackGraph implements HighChart {

    private final String[] builtSeries;

    public ActivityStackGraph(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        this.builtSeries = createSeries(activityData);
    }

    public String toHighChartsLabels() {
        return builtSeries[0];
    }

    @Override
    public String toHighChartsSeries() {
        return builtSeries[1];
    }

    private ActivityStackGraph() {
        throw new IllegalStateException("Utility Class");
    }

    private String[] createSeries(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        String[] groups = ActivityIndex.getGroups();
        String[] colors = Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        int maxCol = colors.length;

        // Series 0 is Labels for Graph x-axis, others are data for each group.
        StringBuilder[] series = new StringBuilder[groups.length + 1];
        for (int i = 0; i <= groups.length; i++) {
            series[i] = new StringBuilder();
        }
        for (int i = 1; i <= groups.length; i++) {
            series[i] = new StringBuilder("{name: '")
                    .append(groups[i - 1])
                    .append("',color:").append(colors[(i - 1) % maxCol])
                    .append(",data: [");
        }

        int size = activityData.size();
        int i = 0;
        for (Long date : activityData.navigableKeySet()) {
            Map<String, Set<UUID>> data = activityData.get(date);

            series[0].append("'").append(FormatUtils.formatTimeStamp(date)).append("'");
            for (int j = 1; j <= groups.length; j++) {
                Set<UUID> players = data.get(groups[j - 1]);
                series[j].append(players != null ? players.size() : 0);
            }

            if (i < size - 1) {
                for (int j = 0; j <= groups.length; j++) {
                    series[j].append(",");
                }
            }
            i++;
        }

        StringBuilder seriesBuilder = new StringBuilder("[");

        for (int j = 1; j <= groups.length; j++) {
            seriesBuilder.append(series[j].append("]}").toString());
            if (j < groups.length) {
                seriesBuilder.append(",");
            }
        }

        return new String[]{series[0].toString(), seriesBuilder.append("]").toString()};
    }
}