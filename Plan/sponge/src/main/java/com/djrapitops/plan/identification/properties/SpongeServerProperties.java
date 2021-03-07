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

import org.spongepowered.api.Game;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;

/**
 * ServerProperties for Sponge.
 *
 * @author AuroraLS3
 */
@Singleton
public class SpongeServerProperties extends ServerProperties {

    @Inject
    public SpongeServerProperties(Game game) {
        super(
                "Sponge",
                game.getServer().getBoundAddress().orElseGet(() -> new InetSocketAddress(25565)).getPort(),
                game.getPlatform().getMinecraftVersion().getName(),
                game.getPlatform().getMinecraftVersion().getName(),
                () -> game.getServer().getBoundAddress()
                        .orElseGet(() -> new InetSocketAddress(25565))
                        .getAddress().getHostAddress(),
                game.getServer().getMaxPlayers()
        );
    }
}