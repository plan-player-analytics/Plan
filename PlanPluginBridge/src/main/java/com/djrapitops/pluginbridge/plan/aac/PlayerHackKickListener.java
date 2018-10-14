/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.aac;

import com.djrapitops.plan.system.processing.Processing;
import me.konsolas.aac.api.AACAPIProvider;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationCommandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.UUID;

/**
 * Class responsible for listening kick events made by AAC.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
class PlayerHackKickListener implements Listener {

    private final HackerTable hackerTable;
    private final Processing processing;

    PlayerHackKickListener(HackerTable hackerTable, Processing processing) {
        this.hackerTable = hackerTable;
        this.processing = processing;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerViolationCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        HackType hackType = event.getHackType();
        long time = System.currentTimeMillis();
        int violations = AACAPIProvider.getAPI().getViolationLevel(player, hackType);

        HackObject hackObject = new HackObject(uuid, time, hackType, violations);

        processing.submitNonCritical(() -> hackerTable.insertHackRow(hackObject));
    }
}
