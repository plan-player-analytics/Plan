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
package com.djrapitops.plan.identification.properties;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ProxySettings;
import net.md_5.bungee.api.ProxyServer;

/**
 * ServerProperties for Bungee.
 * <p>
 * Supports RedisBungee for Players online getting.
 *
 * @author AuroraLS3
 */
public class BungeeServerProperties extends ServerProperties {

    public BungeeServerProperties(ProxyServer server, PlanConfig config) {
        super(
                "BungeeCord",
                -1,
                server.getVersion(),
                server.getVersion(),
                () -> config.get(ProxySettings.IP),
                server.getConfig().getPlayerLimit()
        );
    }
}