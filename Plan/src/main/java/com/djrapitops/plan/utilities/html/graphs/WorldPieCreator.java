package main.java.com.djrapitops.plan.utilities.html.graphs;

import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.data.time.WorldTimes;

import java.util.Map;
import java.util.stream.Collectors;

public class WorldPieCreator {

    private WorldPieCreator() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * Used to create HighCharts series string for series & drilldown.
     *
     * @param worldTimes WorldTimes object.
     * @return String array, index 0: Series data, 1: drilldown data
     */
    public static String[] createSeriesData(WorldTimes worldTimes) {
        StringBuilder seriesBuilder = new StringBuilder("[");
        int i = 0;
        Map<String, Long> playtimePerWorld = worldTimes.getWorldTimes().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getTotal()));

        int size = playtimePerWorld.size();
        for (Map.Entry<String, Long> world : playtimePerWorld.entrySet()) {
            String worldName = world.getKey();
            seriesBuilder.append("{name:'").append(worldName)
                    .append("',y:").append(world.getValue())
                    .append(",drilldown: '").append(worldName).append("'");

            seriesBuilder.append("}");
            if (i < size - 1) {
                seriesBuilder.append(",");
            }
            i++;
        }
        seriesBuilder.append("]");

        String seriesData = seriesBuilder.toString();

        String drilldownData = createDrilldownData(worldTimes);

        return new String[]{seriesData, drilldownData};
    }

    private static String createDrilldownData(WorldTimes worldTimes) {
        StringBuilder drilldownBuilder = new StringBuilder();
        int i = 0;

        Map<String, GMTimes> gmTimesMap = worldTimes.getWorldTimes();
        if (gmTimesMap.isEmpty()) {
            return "[{[]}]";
        }
        int size = gmTimesMap.size();
        drilldownBuilder.append("[");
        for (Map.Entry<String, GMTimes> world : gmTimesMap.entrySet()) {
            drilldownBuilder.append("{name:'").append(world.getKey())
                    .append("', id:'").append(world.getKey())
                    .append("',");
            drilldownBuilder.append("data: [");

            appendGMTimesForWorld(drilldownBuilder, world);

            if (i < size - 1) {
                drilldownBuilder.append(",");
            }
            i++;
        }
        drilldownBuilder.append("]");
        return drilldownBuilder.toString();
    }

    private static void appendGMTimesForWorld(StringBuilder drilldownBuilder, Map.Entry<String, GMTimes> world) {
        Map<String, Long> gmTimes = world.getValue().getTimes();
        int smallSize = gmTimes.size();
        int j = 0;
        for (Map.Entry<String, Long> entry : gmTimes.entrySet()) {
            Long time = entry.getValue();
            if (time == 0) {
                j++;
                continue;
            }
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
