/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.webserver.cache;

import java.util.UUID;

/**
 * Enum class for "magic" ResponseCache identifier values.
 *
 * @author Rsl1122
 */
public enum PageId {

    SERVER("serverPage:"),
    RAW_SERVER("rawServer:"),
    PLAYER("playerPage:"),
    RAW_PLAYER("rawPlayer:"),
    PLAYERS("playersPage"),

    ERROR("error:"),
    FORBIDDEN(ERROR.of("Forbidden")),
    NOT_FOUND(ERROR.of("Not Found")),

    JS("js:"),
    CSS("css:"),

    FAVICON("Favicon"),

    PLAYER_PLUGINS_TAB("playerPluginsTab:"),
    NETWORK_CONTENT("networkContent");

    private final String id;

    PageId(String id) {
        this.id = id;
    }

    public String of(String additionalInfo) {
        return id + additionalInfo;
    }

    public String of(UUID uuid) {
        return of(uuid.toString());
    }

    public String id() {
        return id;
    }
}
