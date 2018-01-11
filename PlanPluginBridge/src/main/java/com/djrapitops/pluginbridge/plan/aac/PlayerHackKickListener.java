/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.djrapitops.pluginbridge.plan.aac;


import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.database.databases.SQLDB;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.utilities.MiscUtils;
import me.konsolas.aac.api.AACAPIProvider;
import me.konsolas.aac.api.HackType;
import me.konsolas.aac.api.PlayerViolationCommandEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Class responsible for listening kick events made by AAC.
 *
 * @author Rsl1122
 * @since 4.1.0
 */
public class PlayerHackKickListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onKick(PlayerViolationCommandEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        HackType hackType = event.getHackType();
        long time = MiscUtils.getTime();
        int violations = AACAPIProvider.getAPI().getViolationLevel(player, hackType);

        HackObject hackObject = new HackObject(uuid, time, hackType, violations);
        Plan plan = Plan.getInstance();
        plan.addToProcessQueue(new Processor<UUID>(uuid) {
            @Override
            public void process() {
                try {
                    new HackerTable((SQLDB) plan.getDB()).insertHackRow(hackObject);
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                }
            }
        });
    }
}
