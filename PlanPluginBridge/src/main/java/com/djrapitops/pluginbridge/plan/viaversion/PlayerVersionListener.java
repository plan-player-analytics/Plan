/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.viaversion;

import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.systems.processing.Processor;
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

    private final Plan plan;
    private final ViaAPI viaAPI;
    private final ProtocolTable table;

    public PlayerVersionListener(Plan plan, ViaAPI viaAPI, ProtocolTable dbTable) {
        this.plan = plan;
        this.viaAPI = viaAPI;
        this.table = dbTable;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        int playerVersion = viaAPI.getPlayerVersion(uuid);
        plan.addToProcessQueue(new Processor<UUID>(uuid) {
            @Override
            public void process() {
                try {
                    table.saveProtocolVersion(uuid, playerVersion);
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName() + ":PlanViaVersionJoinListener", e);
                }
            }
        });
    }
}
