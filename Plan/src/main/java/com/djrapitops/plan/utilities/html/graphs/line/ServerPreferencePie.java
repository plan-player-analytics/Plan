package main.java.com.djrapitops.plan.utilities.html.graphs.line;

import main.java.com.djrapitops.plan.data.time.WorldTimes;

import java.util.Map;
import java.util.UUID;

public class ServerPreferencePie {

    private ServerPreferencePie() {
        throw new IllegalStateException("Utility Class");
    }

    public static String createSeries(Map<UUID, String> serverNames, Map<UUID, WorldTimes> serverWorldTimes) {
        StringBuilder seriesBuilder = new StringBuilder("[");
        int i = 0;
        int size = serverWorldTimes.size();
        for (Map.Entry<UUID, WorldTimes> server : serverWorldTimes.entrySet()) {
            String serverName = serverNames.getOrDefault(server.getKey(), "Unknown");
            seriesBuilder.append("{name:'").append(serverName)
                    .append("',y:").append(server.getValue().getTotal());

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
