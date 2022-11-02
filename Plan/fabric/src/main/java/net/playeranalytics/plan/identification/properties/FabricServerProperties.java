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
package net.playeranalytics.plan.identification.properties;

import com.djrapitops.plan.identification.properties.ServerProperties;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;

/**
 * server.properties fetcher for Fabric
 *
 * @author Kopo942
 */
public class FabricServerProperties extends ServerProperties {

    public FabricServerProperties(MinecraftDedicatedServer server) {
        super(
                "Fabric",
                server.getServerPort(),
                server.getVersion(),
                FabricLoader.getInstance().getModContainer("fabric").map(container -> container.getMetadata().getVersion().getFriendlyString()).orElse("Unknown") +
                        " (API), " +
                        FabricLoader.getInstance().getModContainer("fabricloader").map(modContainer -> modContainer.getMetadata().getVersion().getFriendlyString()).orElse("Unknown") +
                        " (loader)",
                () -> (server.getServerIp() == null) ? "" : server.getServerIp(),
                server.getProperties().maxPlayers
        );
    }
}
