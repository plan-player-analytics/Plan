package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.WorldTimes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerPreferencePie extends AbstractPieChart {

    public ServerPreferencePie(Map<UUID, String> serverNames, Map<UUID, WorldTimes> serverWorldTimes) {
        super(turnToSlices(serverNames, serverWorldTimes));
    }

    private static List<PieSlice> turnToSlices(Map<UUID, String> serverNames, Map<UUID, WorldTimes> serverWorldTimes) {
        List<PieSlice> slices = new ArrayList<>();

        for (Map.Entry<UUID, WorldTimes> server : serverWorldTimes.entrySet()) {
            UUID serverUUID = server.getKey();
            WorldTimes worldTimes = server.getValue();

            String serverName = serverNames.getOrDefault(serverUUID, "Unknown");
            long num = worldTimes.getTotal();

            slices.add(new PieSlice(serverName, num));
        }

        return slices;
    }
}
