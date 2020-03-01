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
package com.djrapitops.plan.delivery.rendering.json.graphs.pie;

import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.utilities.comparators.PieSliceComparator;

import java.util.*;

public class WorldPie extends PieWithDrilldown {

    private final Map<String, GMTimes> gmTimesAliasMap;

    WorldPie(
            Map<String, Long> playtimePerAlias,
            Map<String, GMTimes> gmTimesAliasMap,
            String[] colors,
            boolean orderByPercentage
    ) {
        super(turnIntoSlices(playtimePerAlias, colors));

        this.gmTimesAliasMap = gmTimesAliasMap;

        if (orderByPercentage) {
            slices.sort(new PieSliceComparator());
        }
    }

    private static List<PieSlice> turnIntoSlices(Map<String, Long> playtimePerAlias, String[] colors) {
        int colLength = colors.length;

        List<String> worlds = new ArrayList<>(playtimePerAlias.keySet());
        Collections.sort(worlds);

        List<PieSlice> slices = new ArrayList<>();
        int i = 0;
        for (String alias : worlds) {
            Long value = playtimePerAlias.getOrDefault(alias, 0L);
            if (value != 0L) {
                slices.add(new PieSlice(alias, value, colors[i % colLength], true));
            }
            i++;
        }

        return slices;
    }

    public List<Map<String, Object>> toHighChartsDrillDownMaps() {
        List<Map<String, Object>> drilldowns = new ArrayList<>();
        for (Map.Entry<String, GMTimes> worldAlias : gmTimesAliasMap.entrySet()) {
            Map<String, Object> drilldown = new HashMap<>();
            drilldown.put("name", worldAlias.getKey());
            drilldown.put("id", worldAlias.getKey());
            drilldown.put("data", createGMTimesForWorld(worldAlias.getValue()));
            drilldowns.add(drilldown);
        }
        return drilldowns;
    }

    private List<List<Object>> createGMTimesForWorld(GMTimes gmTimes) {
        List<List<Object>> data = new ArrayList<>();
        for (Map.Entry<String, Long> gmEntry : gmTimes.getTimes().entrySet()) {
            List<Object> gmList = Arrays.asList(gmEntry.getKey(), gmEntry.getValue());
            data.add(gmList);
        }
        return data;
    }
}
