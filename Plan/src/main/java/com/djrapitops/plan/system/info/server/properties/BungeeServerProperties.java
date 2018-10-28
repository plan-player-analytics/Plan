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
package com.djrapitops.plan.system.info.server.properties;

import net.md_5.bungee.api.ProxyServer;

/**
 * ServerProperties for Bungee.
 * <p>
 * Supports RedisBungee for Players online getting.
 *
 * @author Rsl1122
 */
public class BungeeServerProperties extends ServerProperties {

    public BungeeServerProperties(ProxyServer server, String ip) {
        super(
                server.getServers().toString(),
                "BungeeCord",
                -1,
                server.getVersion(),
                server.getVersion(),
                () -> ip,
                server.getConfig().getPlayerLimit(),
                RedisCheck.isClassAvailable() ? new RedisPlayersOnlineSupplier() : server::getOnlineCount
        );
    }
}