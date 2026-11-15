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
package com.djrapitops.plan.delivery.rendering.json.datapoint.types.playtime;

import com.djrapitops.plan.delivery.domain.datatransfer.GenericFilter;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.database.queries.objects.SessionQueries;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utility for getting playtime per server name based on a filter.
 *
 * @author AuroraLS3
 */
@Singleton
public class ServerPlaytimeForFilter {

    private final DBSystem dbSystem;

    @Inject
    public ServerPlaytimeForFilter(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    private static Map<String, Long> getPlaytimeForServer(Database db, long after, long before, List<ServerUUID> serverUUIDs) {
        Map<String, Long> playtimeByServerName = db.query(SessionQueries.playtimePerServer(after, before));
        if (serverUUIDs.isEmpty()) {
            return playtimeByServerName;
        }
        
        Map<ServerUUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        Map<String, Long> filtered = new HashMap<>();
        for (ServerUUID uuid : serverUUIDs) {
            String name = serverNames.get(uuid);
            if (name != null && playtimeByServerName.containsKey(name)) {
                filtered.put(name, playtimeByServerName.get(name));
            }
        }
        return filtered;
    }

    private static Map<String, Long> getPlaytimeForPlayer(Database db, long after, long before, UUID playerUUID, List<ServerUUID> serverUUIDs) {
        Map<ServerUUID, Long> playtimeByUuid = db.query(SessionQueries.playtimeOfPlayer(after, before, playerUUID));
        Map<ServerUUID, String> serverNames = db.query(ServerQueries.fetchServerNames());
        Map<String, Long> playtimeByServerName = new HashMap<>();
        for (Map.Entry<ServerUUID, Long> entry : playtimeByUuid.entrySet()) {
            ServerUUID uuid = entry.getKey();
            if (serverUUIDs.isEmpty() || serverUUIDs.contains(uuid)) {
                playtimeByServerName.put(serverNames.getOrDefault(uuid, uuid.toString()), entry.getValue());
            }
        }
        return playtimeByServerName;
    }

    public Map<String, Long> getPlaytimePerServer(GenericFilter filter) {
        Database db = dbSystem.getDatabase();
        long after = filter.getAfter();
        long before = filter.getBefore();
        List<ServerUUID> serverUUIDs = filter.getServerUUIDs();

        return filter.getPlayerUUID()
                .map(playerUUID -> getPlaytimeForPlayer(db, after, before, playerUUID, serverUUIDs))
                .orElseGet(() -> getPlaytimeForServer(db, after, before, serverUUIDs));
    }
}
