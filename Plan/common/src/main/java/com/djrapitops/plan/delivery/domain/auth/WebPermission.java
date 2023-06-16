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

/**
 * List of web permissions.
 *
 * @author AuroraLS3
 */
public enum WebPermission {
    PAGE_NETWORK,
    PAGE_SERVER,
    PAGE_SERVER_OVERVIEW,
    PAGE_SERVER_ONLINE_OVERVIEW,
    PAGE_PLAYER,
    PAGE_PLAYERS,
    PAGE_QUERY,

    ACCESS_PLAYER,
    ACCESS_PLAYER_SELF,
    // Restricting to specific servers: access.server.uuid
    ACCESS_SERVER;

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
}
