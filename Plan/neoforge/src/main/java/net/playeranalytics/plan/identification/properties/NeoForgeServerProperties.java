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
import net.minecraft.server.dedicated.DedicatedServer;
import net.neoforged.fml.ModList;

/**
 * server.properties fetcher for NeoForge.
 */
public class NeoForgeServerProperties extends ServerProperties {

    public NeoForgeServerProperties(DedicatedServer server) {
        super(
                "NeoForge",
                server.getPort(),
                server.getServerVersion(),
                ModList.get().getModContainerById("neoforge")
                        .map(container -> container.getModInfo().getVersion().toString())
                        .orElse("Unknown")
                        + " (NeoForge), "
                        + ModList.get().getModContainerById("fabric_api")
                        .map(container -> container.getModInfo().getVersion().toString())
                        .orElse("Unknown")
                        + " (FFAPI)",
                () -> (server.getLocalIp() == null) ? "" : server.getLocalIp(),
                server.getProperties().maxPlayers.get()
        );
    }
}
