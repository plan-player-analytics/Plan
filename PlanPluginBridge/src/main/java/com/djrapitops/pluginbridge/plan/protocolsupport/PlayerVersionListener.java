/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.protocolsupport;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processor;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.pluginbridge.plan.viaversion.ProtocolTable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import protocolsupport.api.ProtocolSupportAPI;
import protocolsupport.api.ProtocolVersion;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
public class PlayerVersionListener implements Listener {

    public PlayerVersionListener() {
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ProtocolVersion protocolVersion = ProtocolSupportAPI.getProtocolVersion(player);
        int playerVersion = protocolVersion.getId();
        Plan plan = Plan.getInstance();
        Processor.queue(() -> {
            try {
                new ProtocolTable((SQLDB) Database.getActive()).saveProtocolVersion(uuid, playerVersion);
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName(), e);
            }
        });
    }
}
