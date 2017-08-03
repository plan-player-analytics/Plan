package main.java.com.djrapitops.plan.ui.html.graphs;

import java.util.Map;

public class WorldPieCreator {

    private WorldPieCreator() {
        throw new IllegalStateException("Utility Class");
    }

    public static String createSeriesData(Map<String, Long> worldTimes) {
        StringBuilder arrayBuilder = new StringBuilder("[");
        int i = 0;
        int size = worldTimes.size();
        for (Map.Entry<String, Long> world : worldTimes.entrySet()) {
            arrayBuilder.append("{name:'").append(world.getKey())
                    .append("',y:").append(world.getValue());

            if (i == 1) {
                arrayBuilder.append(", sliced: true, selected: true");
            }

            arrayBuilder.append("}");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
            i++;
        }
        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }
}
