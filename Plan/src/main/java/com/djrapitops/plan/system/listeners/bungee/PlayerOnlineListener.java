/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.listeners.bungee;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.BungeePlayerRegisterProcessor;
import com.djrapitops.plan.system.processing.processors.player.IPUpdateProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.UUID;

/**
 * Player Join listener for Bungee.
 *
 * @author Rsl1122
 */
public class PlayerOnlineListener implements Listener {

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            String ip = player.getAddress().getAddress().getHostAddress();
            long now = System.currentTimeMillis();

            Processing.submit(new BungeePlayerRegisterProcessor(uuid, name, now,
                    new IPUpdateProcessor(uuid, ip, now))
            );
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
