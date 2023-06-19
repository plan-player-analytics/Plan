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
package com.djrapitops.plan.delivery.domain.auth;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Supplier;

/**
 * List of web permissions.
 *
 * @author AuroraLS3
 */
public enum WebPermission implements Supplier<String> {
    PAGE_NETWORK,
    PAGE_NETWORK_OVERVIEW,
    PAGE_NETWORK_OVERVIEW_GRAPHS,
    PAGE_NETWORK_OVERVIEW_GRAPHS_ONLINE,
    PAGE_NETWORK_OVERVIEW_GRAPHS_DAY_BY_DAY,
    PAGE_NETWORK_OVERVIEW_GRAPHS_HOUR_BY_HOUR,
    PAGE_NETWORK_SERVER_LIST,
    PAGE_NETWORK_PLAYERBASE,
    PAGE_NETWORK_PLAYERBASE_OVERVIEW,
    PAGE_NETWORK_PLAYERBASE_GRAPHS,
    PAGE_NETWORK_SESSIONS,
    PAGE_NETWORK_SESSIONS_OVERVIEW,
    PAGE_NETWORK_SESSIONS_WORLD_PIE,
    PAGE_NETWORK_SESSIONS_SERVER_PIE,
    PAGE_NETWORK_SESSIONS_LIST,
    PAGE_NETWORK_JOIN_ADDRESSES,
    PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_PIE,
    PAGE_NETWORK_JOIN_ADDRESSES_GRAPHS_TIME,
    PAGE_NETWORK_RETENTION,
    PAGE_NETWORK_GEOLOCATIONS,
    PAGE_NETWORK_GEOLOCATIONS_MAP,
    PAGE_NETWORK_GEOLOCATIONS_PING_PER_COUNTRY,
    PAGE_NETWORK_PLAYERS,
    PAGE_NETWORK_PERFORMANCE,
    PAGE_NETWORK_PLUGINS,

    PAGE_SERVER,
    PAGE_SERVER_OVERVIEW,
    PAGE_SERVER_OVERVIEW_PLAYERS_ONLINE_GRAPH,
    PAGE_SERVER_ONLINE_ACTIVITY,
    PAGE_SERVER_ONLINE_ACTIVITY_OVERVIEW,
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS,
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_DAY_BY_DAY,
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_HOUR_BY_HOUR,
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_PUNCHCARD,
    PAGE_SERVER_ONLINE_ACTIVITY_GRAPHS_CALENDAR,
    PAGE_SERVER_PLAYERBASE,
    PAGE_SERVER_PLAYERBASE_OVERVIEW,
    PAGE_SERVER_PLAYERBASE_GRAPHS,
    PAGE_SERVER_PLAYER_VERSUS,
    PAGE_SERVER_PLAYER_VERSUS_OVERVIEW,
    PAGE_SERVER_PLAYER_VERSUS_KILL_LIST,
    PAGE_SERVER_PLAYERS,
    PAGE_SERVER_SESSIONS,
    PAGE_SERVER_SESSIONS_OVERVIEW,
    PAGE_SERVER_SESSIONS_WORLD_PIE,
    PAGE_SERVER_SESSIONS_LIST,
    PAGE_SERVER_JOIN_ADDRESSES,
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS,
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_PIE,
    PAGE_SERVER_JOIN_ADDRESSES_GRAPHS_TIME,
    PAGE_SERVER_RETENTION,
    PAGE_SERVER_GEOLOCATIONS,
    PAGE_SERVER_GEOLOCATIONS_MAP,
    PAGE_SERVER_GEOLOCATIONS_PING_PER_COUNTRY,
    PAGE_SERVER_PERFORMANCE,
    PAGE_SERVER_PERFORMANCE_GRAPHS,
    PAGE_SERVER_PERFORMANCE_OVERVIEW,
    PAGE_SERVER_PLUGINS,

    PAGE_PLAYER,
    PAGE_PLAYER_OVERVIEW,
    PAGE_PLAYER_SESSIONS,
    PAGE_PLAYER_VERSUS,
    PAGE_PLAYER_SERVERS,
    PAGE_PLAYER_PLUGINS,

    ACCESS_PLAYER,
    ACCESS_PLAYER_SELF,
    // Restricting to specific servers: access.server.uuid
    ACCESS_SERVER,
    ACCESS_NETWORK,
    ACCESS_PLAYERS,
    ACCESS_QUERY,
    ACCESS_ERRORS,
    ACCESS_DOCS,

    MANAGE_GROUPS,
    MANAGE_USERS;

    private final boolean deprecated;

    WebPermission() {
        this(false);
    }

    WebPermission(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getPermission() {
        return StringUtils.lowerCase(name()).replace('_', '.');
    }

    public boolean isDeprecated() {
        return deprecated;
    }


    @Override
    public String get() {
        return getPermission();
    }
}
