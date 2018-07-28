/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.graphs.stack.AbstractStackGraph;
import com.djrapitops.plan.utilities.html.graphs.stack.StackDataSet;

import java.util.*;

/**
 * Stack Graph that represents evolution of the PlayerBase in terms of ActivityIndex Groups.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
public class ActivityStackGraph extends AbstractStackGraph {

    public ActivityStackGraph(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        super(getLabels(activityData.navigableKeySet()), getDataSets(activityData));
    }

    public ActivityStackGraph(long date, PlayersMutator mutator) {
        this(mutator.toActivityDataMap(date));
    }

    private static String[] getLabels(NavigableSet<Long> dates) {
        return dates.stream()
                .map(FormatUtils::formatTimeStampDay)
                .toArray(String[]::new);
    }

    private static StackDataSet[] getDataSets(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        String[] groups = ActivityIndex.getGroups();
        String[] colors = Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
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
