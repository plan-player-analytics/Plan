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
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
@Singleton
public class PlayerVersionListener implements Listener {

    private final Processing processing;
    private final ProtocolTable protocolTable;

    @Inject
    public PlayerVersionListener(
            Processing processing,
            ProtocolTable protocolTable
    ) {
        this.processing = processing;
        this.protocolTable = protocolTable;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        int playerVersion = protocolVersion.getId();
        processing.submitNonCritical(() -> protocolTable.saveProtocolVersion(uuid, playerVersion));
    }
}
