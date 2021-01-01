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

import java.util.UUID;

/**
 * Enum for different JSON data entries that can be stored in {@link JSONCache}.
 *
 * @author Rsl1122
 */
public enum DataID {
    PLAYERS,
    SESSIONS,
    SERVERS,
    KILLS,
    PING_TABLE,
    GRAPH_PERFORMANCE,
    GRAPH_ONLINE,
    GRAPH_UNIQUE_NEW,
    GRAPH_HOURLY_UNIQUE_NEW,
    GRAPH_CALENDAR,
    GRAPH_WORLD_PIE,
    GRAPH_WORLD_MAP,
    GRAPH_ACTIVITY,
    GRAPH_PING,
    GRAPH_SERVER_PIE,
    GRAPH_HOSTNAME_PIE,
    GRAPH_PUNCHCARD,
    SERVER_OVERVIEW,
    ONLINE_OVERVIEW,
    SESSIONS_OVERVIEW,
    PVP_PVE,
    PLAYERBASE_OVERVIEW,
    PERFORMANCE_OVERVIEW,
    EXTENSION_NAV,
    EXTENSION_TABS
    ;

    public String of(UUID serverUUID) {
        return name() + '-' + serverUUID;
    }

}
