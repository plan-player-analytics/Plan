/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;

import java.util.*;

/**
 * Pie about different Activity Groups defined by ActivityIndex.
 *
 * @author Rsl1122
 * @see ActivityIndex
 * @since 4.2.0
 */
public class ActivityPie extends Pie {

    ActivityPie(Map<String, Set<UUID>> activityData, String[] colors) {
        super(turnToSlices(activityData, colors));
    }

    private static List<PieSlice> turnToSlices(Map<String, Set<UUID>> activityData, String[] colors) {
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
