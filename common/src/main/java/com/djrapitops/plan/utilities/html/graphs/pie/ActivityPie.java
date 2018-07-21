/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;

import java.util.*;

/**
 * Pie about different Activity Groups defined by ActivityIndex.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
public class ActivityPie extends AbstractPieChart {

    public ActivityPie(Map<String, Set<UUID>> activityData) {
        super(turnToSlices(activityData));
    }

    private static List<PieSlice> turnToSlices(Map<String, Set<UUID>> activityData) {
        String[] colors = Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        int maxCol = colors.length;

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String group : ActivityIndex.getGroups()) {
            Set<UUID> players = activityData.getOrDefault(group, new HashSet<>());
            int num = players.size();

            slices.add(new PieSlice(group, num, colors[i % maxCol], false));
            i++;
        }

        return slices;
    }
}
