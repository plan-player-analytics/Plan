/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
