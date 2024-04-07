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
package com.djrapitops.plan.delivery.webserver.cache;

import com.djrapitops.plan.identification.ServerUUID;

/**
 * Enum for different JSON data entries that can be stored in cache.
 *
 * @author AuroraLS3
 */
public enum DataID {
    PLAYERS,
    PLAYERS_V2,
    SESSIONS,
    SERVERS,
    KILLS,
    PING_TABLE,
    GRAPH_PERFORMANCE,
    GRAPH_OPTIMIZED_PERFORMANCE,
    GRAPH_ONLINE,
    GRAPH_ONLINE_PROXIES,
    GRAPH_UNIQUE_NEW,
    GRAPH_HOURLY_UNIQUE_NEW,
    GRAPH_CALENDAR,
    GRAPH_WORLD_PIE,
    GRAPH_WORLD_MAP,
    GRAPH_ACTIVITY,
    GRAPH_PING,
    GRAPH_SERVER_PIE,
    GRAPH_PUNCHCARD,
    SERVER_OVERVIEW,
    ONLINE_OVERVIEW,
    SESSIONS_OVERVIEW,
    PVP_PVE,
    PLAYERBASE_OVERVIEW,
    PERFORMANCE_OVERVIEW,
    EXTENSION_NAV,
    EXTENSION_TABS,
    EXTENSION_JSON,
    LIST_SERVERS,
    JOIN_ADDRESSES_BY_DAY(false),
    PLAYER_RETENTION,
    PLAYER_JOIN_ADDRESSES,
    PLAYER_ALLOWLIST_BOUNCES,
    ;

    private final boolean cacheable;

    DataID() {
        this(true);
    }

    DataID(boolean cacheable) {
        this.cacheable = cacheable;
    }

    public boolean isCacheable() {
        return cacheable;
    }

    public String of(ServerUUID serverUUID) {
        if (serverUUID == null) return name();
        return name() + "_" + serverUUID;
    }
}
