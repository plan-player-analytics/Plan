/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.bungee.listeners.bungee;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.BungeePlayerRegisterProcessor;
import com.djrapitops.plan.system.processing.processors.player.IPUpdateProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.net.InetAddress;
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
            InetAddress address = player.getAddress().getAddress();
            long now = System.currentTimeMillis();

            SessionCache.getInstance().cacheSession(uuid, new Session(uuid, now, "", ""));

            Processing.submit(new BungeePlayerRegisterProcessor(uuid, name, now,
                    new IPUpdateProcessor(uuid, address, now))
            );
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @EventHandler
    public void onLogout(ServerDisconnectEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            SessionCache.getInstance().endSession(uuid, System.currentTimeMillis());
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            long now = System.currentTimeMillis();
            // Replaces the current session in the cache.
            SessionCache.getInstance().cacheSession(uuid, new Session(uuid, now, "", ""));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
