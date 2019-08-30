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
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.system.gathering.domain.GMTimes;
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
            drilldown.put("data", parseGMTimesForWorld(worldAlias.getValue()));
            drilldowns.add(drilldown);
        }
        return drilldowns;
    }

    private List<List> parseGMTimesForWorld(GMTimes gmTimes) {
        List<List> data = new ArrayList<>();
        for (Map.Entry<String, Long> gmEntry : gmTimes.getTimes().entrySet()) {
            List gmList = Arrays.asList(gmEntry.getKey(), gmEntry.getValue());
            data.add(gmList);
        }
        return data;
    }

    @Override
    public String toHighChartsDrilldown() {
        StringBuilder drilldownBuilder = new StringBuilder();
        int i = 0;

        if (gmTimesAliasMap.isEmpty()) {
            return "[]";
        }
        int size = gmTimesAliasMap.size();

        drilldownBuilder.append("[");
        for (Map.Entry<String, GMTimes> worldAlias : gmTimesAliasMap.entrySet()) {
            drilldownBuilder.append("{name:'").append(worldAlias.getKey())
                    .append("', id:'").append(worldAlias.getKey())
                    .append("',colors: gmPieColors,");
            drilldownBuilder.append("data: [");

            appendGMTimesForWorld(drilldownBuilder, worldAlias);

            if (i < size - 1) {
                drilldownBuilder.append(",");
            }
            i++;
        }
        drilldownBuilder.append("]");
        return drilldownBuilder.toString();
    }

    private void appendGMTimesForWorld(StringBuilder drilldownBuilder, Map.Entry<String, GMTimes> world) {
        Map<String, Long> gmTimes = world.getValue().getTimes();
        int smallSize = gmTimes.size();
        int j = 0;
        for (Map.Entry<String, Long> entry : gmTimes.entrySet()) {
            Long time = entry.getValue();
            drilldownBuilder.append("['")
                    .append(entry.getKey())
                    .append("',")
                    .append(time)
                    .append("]");

            if (j < smallSize - 1) {
                drilldownBuilder.append(",");
            }
            j++;
        }
        drilldownBuilder.append("]}");
    }
}
