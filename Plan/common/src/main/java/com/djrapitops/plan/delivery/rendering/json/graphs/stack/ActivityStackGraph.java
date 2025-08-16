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
package com.djrapitops.plan.delivery.rendering.json.graphs.stack;

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Stack Graph that represents evolution of the PlayerBase in terms of ActivityIndex Groups.
 *
 * @author AuroraLS3
 * @see ActivityIndex
 */
class ActivityStackGraph extends StackGraph {

    ActivityStackGraph(DateMap<Map<String, Integer>> activityData, String[] colors, String[] groups) {
        super(getLabels(activityData.navigableKeySet()), getDataSets(activityData, colors, groups));
    }

    private static Serializable[] getLabels(Collection<Long> dates) {
        return dates.toArray(Serializable[]::new);
    }

    private static StackDataSet[] initializeDataSet(String[] groups, String[] colors) {
        int maxCol = colors.length;
        StackDataSet[] dataSets = new StackDataSet[groups.length];

        for (int i = 0; i < groups.length; i++) {
            dataSets[i] = new StackDataSet(new ArrayList<>(), groups[i], colors[i % maxCol]);
        }

        return dataSets;
    }

    private static StackDataSet[] getDataSets(DateMap<Map<String, Integer>> activityData, String[] colors, String[] groups) {
        StackDataSet[] dataSets = initializeDataSet(groups, colors);
        String[] defaultGroups = ActivityIndex.getDefaultGroups();

        for (Map<String, Integer> data : activityData.values()) {
            for (int j = 0; j < groups.length; j++) {
                dataSets[j].add(data.getOrDefault(defaultGroups[j], 0));
            }
        }

        return dataSets;
    }
}
