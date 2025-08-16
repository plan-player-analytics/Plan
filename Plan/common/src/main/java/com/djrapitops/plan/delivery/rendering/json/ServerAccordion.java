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
package com.djrapitops.plan.delivery.rendering.json;

import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.container.PerServerContainer;
import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PerServerKeys;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.rendering.json.graphs.Graphs;
import com.djrapitops.plan.delivery.rendering.json.graphs.pie.WorldPie;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for creating JSON for Server Accordion
 *
 * @author AuroraLS3
 */
public class ServerAccordion {

    private final Map<ServerUUID, String> serverNames;
    private final PerServerContainer perServer;
    private final String unknown;

    private final Graphs graphs;

    public ServerAccordion(
            PlayerContainer container, Map<ServerUUID, String> serverNames,
            Graphs graphs,
            String unknown
    ) {
        this.graphs = graphs;

        this.serverNames = serverNames;
        perServer = container.getValue(PlayerKeys.PER_SERVER)
                .orElse(new PerServerContainer());
        this.unknown = unknown;
    }

    public List<Map<String, Object>> asMaps() {
        List<Map<String, Object>> servers = new ArrayList<>();

        for (Map.Entry<ServerUUID, DataContainer> entry : perServer.entrySet()) {
            ServerUUID serverUUID = entry.getKey();
            DataContainer ofServer = entry.getValue();
            Map<String, Object> server = new HashMap<>();

            String serverName = serverNames.getOrDefault(serverUUID, unknown);
            WorldTimes worldTimes = ofServer.getValue(PerServerKeys.WORLD_TIMES).orElse(new WorldTimes());
            SessionsMutator sessionsMutator = SessionsMutator.forContainer(ofServer);

            server.put("server_name", serverName);
            server.put("server_uuid", serverUUID.toString());

            server.put("banned", ofServer.getValue(PerServerKeys.BANNED).orElse(false));
            server.put("operator", ofServer.getValue(PerServerKeys.OPERATOR).orElse(false));
            server.put("registered", ofServer.getValue(PerServerKeys.REGISTERED).orElse(0L));
            server.put("last_seen", sessionsMutator.toLastSeen());
            server.put("join_address", ofServer.getValue(PerServerKeys.JOIN_ADDRESS).orElse("-"));

            server.put("session_count", sessionsMutator.count());
            server.put("playtime", sessionsMutator.toPlaytime());
            server.put("afk_time", sessionsMutator.toAfkTime());
            server.put("session_median", sessionsMutator.toMedianSessionLength());
            server.put("longest_session_length", sessionsMutator.toLongestSessionLength());

            server.put("mob_kills", sessionsMutator.toMobKillCount());
            server.put("player_kills", sessionsMutator.toPlayerKillCount());
            server.put("deaths", sessionsMutator.toDeathCount());

            WorldPie worldPie = graphs.pie().worldPie(worldTimes);
            server.put("world_pie_series", worldPie.getSlices());
            server.put("gm_series", worldPie.toHighChartsDrillDownMaps());

            servers.add(server);
        }
        return servers;
    }
}
