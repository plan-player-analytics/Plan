/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.utilities.html.graphs.stack;

import com.djrapitops.plan.data.store.mutators.ActivityIndex;
import com.djrapitops.plan.data.store.objects.DateMap;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.*;

/**
 * Stack Graph that represents evolution of the PlayerBase in terms of ActivityIndex Groups.
 *
 * @author Rsl1122
 * @see ActivityIndex
 */
class ActivityStackGraph extends StackGraph {

    ActivityStackGraph(TreeMap<Long, Map<String, Set<UUID>>> activityData, String[] colors, Formatter<Long> dayFormatter) {
        super(getLabels(activityData.navigableKeySet(), dayFormatter), getDataSets(activityData, colors));
    }

    ActivityStackGraph(DateMap<Map<String, Integer>> activityData, String[] colors, Formatter<Long> dayFormatter) {
        super(getLabels(activityData.navigableKeySet(), dayFormatter), getDataSets(activityData, colors));
    }

    private static String[] getLabels(Collection<Long> dates, Formatter<Long> dayFormatter) {
        return dates.stream()
                .map(dayFormatter)
                .toArray(String[]::new);
    }

    private static StackDataSet[] initializeDataSet(String[] groups, String[] colors) {
        int maxCol = colors.length;
        StackDataSet[] dataSets = new StackDataSet[groups.length];

        for (int i = 0; i < groups.length; i++) {
            dataSets[i] = new StackDataSet(new ArrayList<>(), groups[i], colors[i % maxCol]);
        }

        return dataSets;
    }

    private static StackDataSet[] getDataSets(TreeMap<Long, Map<String, Set<UUID>>> activityData, String[] colors) {
        String[] groups = ActivityIndex.getGroups();
        StackDataSet[] dataSets = initializeDataSet(groups, colors);

        for (Map<String, Set<UUID>> data : activityData.values()) {
            for (int j = 0; j < groups.length; j++) {
                Set<UUID> players = data.get(groups[j]);
                dataSets[j].add((double) (players != null ? players.size() : 0));
            }
        }

        return dataSets;
    }

    private static StackDataSet[] getDataSets(DateMap<Map<String, Integer>> activityData, String[] colors) {
        String[] groups = ActivityIndex.getGroups();
        StackDataSet[] dataSets = initializeDataSet(groups, colors);

        for (Map<String, Integer> data : activityData.values()) {
            for (int j = 0; j < groups.length; j++) {
                dataSets[j].add((double) data.getOrDefault(groups[j], 0));
            }
        }

        return dataSets;
    }
}
