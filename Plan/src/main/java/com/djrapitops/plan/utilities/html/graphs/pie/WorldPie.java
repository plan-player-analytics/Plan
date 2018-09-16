package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.utilities.comparators.PieSliceComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
