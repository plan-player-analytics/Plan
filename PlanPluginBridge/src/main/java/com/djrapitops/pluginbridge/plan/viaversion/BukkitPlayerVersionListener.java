/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.system.processing.Processing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.myles.ViaVersion.api.ViaAPI;

import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
class BukkitPlayerVersionListener implements Listener {

    private final ViaAPI viaAPI;

    private final ProtocolTable protocolTable;
    private final Processing processing;

    BukkitPlayerVersionListener(
            ViaAPI viaAPI,
            ProtocolTable protocolTable,
            Processing processing
    ) {
        this.viaAPI = viaAPI;
        this.protocolTable = protocolTable;
        this.processing = processing;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        processing.submitNonCritical(() -> protocolTable.saveProtocolVersion(uuid, playerVersion));
    }
}
