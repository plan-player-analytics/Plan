package main.java.com.djrapitops.plan.utilities.html.graphs;

import java.util.Map;

public class ServerPreferencePieCreator {

    private ServerPreferencePieCreator() {
        throw new IllegalStateException("Utility Class");
    }

    public static String createSeriesData(Map<String, Long> serverPlaytimes) {
        StringBuilder seriesBuilder = new StringBuilder("[");
        int i = 0;
        int size = serverPlaytimes.size();
        for (Map.Entry<String, Long> server : serverPlaytimes.entrySet()) {
            String serverName = server.getKey();
            seriesBuilder.append("{name:'").append(serverName)
                    .append("',y:").append(server.getValue());

            seriesBuilder.append("}");
            if (i < size - 1) {
                seriesBuilder.append(",");
            }
            i++;
        }
        seriesBuilder.append("]");

        return seriesBuilder.toString();
    }
}
