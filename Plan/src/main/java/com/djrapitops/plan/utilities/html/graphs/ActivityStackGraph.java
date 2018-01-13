/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.graphs.pie.ActivityPie;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ActivityStackGraph {

    private ActivityStackGraph() {
        throw new IllegalStateException("Utility Class");
    }

    public static String[] createSeries(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        String[] sliceNames = ActivityPie.getSliceNames();
        String[] colors = Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        int maxCol = colors.length;

        StringBuilder[] series = new StringBuilder[sliceNames.length + 1];
        for (int i = 0; i <= sliceNames.length; i++) {
            series[i] = new StringBuilder();
        }
        for (int i = 1; i <= sliceNames.length; i++) {
            series[i] = new StringBuilder("{name: '")
                    .append(sliceNames[i - 1])
                    .append("',color:").append(colors[(i - 1) % maxCol])
                    .append(",data: [");
        }

        int size = activityData.size();
        int i = 0;
        for (Long date : activityData.navigableKeySet()) {
            Map<String, Set<UUID>> data = activityData.get(date);

            series[0].append("'").append(FormatUtils.formatTimeStamp(date)).append("'");
            for (int j = 1; j <= sliceNames.length; j++) {
                Set<UUID> players = data.get(sliceNames[j - 1]);
                series[j].append(players != null ? players.size() : 0);
            }

            if (i < size - 1) {
                for (int j = 0; j <= sliceNames.length; j++) {
                    series[j].append(",");
                }
            }
            i++;
        }

        StringBuilder seriesBuilder = new StringBuilder("[");

        for (int j = 1; j <= sliceNames.length; j++) {
            seriesBuilder.append(series[j].append("]}").toString());
            if (j < sliceNames.length) {
                seriesBuilder.append(",");
            }
        }

        return new String[]{series[0].toString(), seriesBuilder.append("]").toString()};
    }
}