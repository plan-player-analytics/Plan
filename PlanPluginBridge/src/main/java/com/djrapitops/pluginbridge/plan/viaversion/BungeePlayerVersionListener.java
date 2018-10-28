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
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.system.processing.Processing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class BungeePlayerVersionListener implements Listener {

    private final ViaAPI viaAPI;

    private final ProtocolTable protocolTable;
    private final Processing processing;

    BungeePlayerVersionListener(
            ViaAPI viaAPI,
            ProtocolTable protocolTable,
            Processing processing
    ) {
        this.viaAPI = viaAPI;
        this.protocolTable = protocolTable;
        this.processing = processing;
    }

    @EventHandler
    public void onJoin(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        processing.submitNonCritical(() -> protocolTable.saveProtocolVersion(uuid, playerVersion));
    }
}
