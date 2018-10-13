/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.stack;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.*;

/**
 * Stack Graph that represents evolution of the PlayerBase in terms of ActivityIndex Groups.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
class ActivityStackGraph extends StackGraph {

    ActivityStackGraph(TreeMap<Long, Map<String, Set<UUID>>> activityData, String[] colors, Formatter<Long> dayFormatter) {
        super(getLabels(activityData.navigableKeySet(), dayFormatter), getDataSets(activityData, colors));
    }

    private static String[] getLabels(Collection<Long> dates, Formatter<Long> dayFormatter) {
        return dates.stream()
                .map(dayFormatter)
                .toArray(String[]::new);
    }

    private static StackDataSet[] getDataSets(TreeMap<Long, Map<String, Set<UUID>>> activityData, String[] colors) {
        String[] groups = ActivityIndex.getGroups();
        int maxCol = colors.length;
        StackDataSet[] dataSets = new StackDataSet[groups.length];

        for (int i = 0; i < groups.length; i++) {
            dataSets[i] = new StackDataSet(new ArrayList<>(), groups[i], colors[(i) % maxCol]);
        }

        for (Long date : activityData.navigableKeySet()) {
            Map<String, Set<UUID>> data = activityData.get(date);

            for (int j = 0; j < groups.length; j++) {
                Set<UUID> players = data.get(groups[j]);
                dataSets[j].add((double) (players != null ? players.size() : 0));
            }
        }

        return dataSets;
    }
}
