/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;

import java.util.*;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ActivityPie {

    private ActivityPie() {
        throw new IllegalStateException("Utility Class");
    }

    public static String[] getSliceNames() {
        return new String[]{"Very Active", "Active", "Regular", "Irregular", "Inactive"};
    }

    public static String createSeries(Map<String, Set<UUID>> activityData) {
        String[] colors = Theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        int maxCol = colors.length;

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String slice : getSliceNames()) {
            Set<UUID> players = activityData.getOrDefault(slice, new HashSet<>());
            int num = players.size();

            slices.add(new PieSlice(slice, num, colors[i % maxCol], false));
            i++;
        }

        return PieSeries.createSeries(slices);
    }
}