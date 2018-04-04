/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.api.utility.log.Log;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import us.myles.ViaVersion.api.ViaAPI;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Class responsible for listening join events for Version protocol.
 *
 * @author Rsl1122
 * @since 3.5.0
 */
public class PlayerVersionListener implements Listener {

    private ViaAPI viaAPI;

    public PlayerVersionListener(ViaAPI viaAPI) {
        this.viaAPI = viaAPI;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        Processing.submitNonCritical(() -> {
            try {
                new ProtocolTable((SQLDB) Database.getActive()).saveProtocolVersion(uuid, playerVersion);
            } catch (SQLException e) {
                Log.toLog(this.getClass().getName() + ":PlanViaVersionJoinListener", e);
            }
        });
    }
}
