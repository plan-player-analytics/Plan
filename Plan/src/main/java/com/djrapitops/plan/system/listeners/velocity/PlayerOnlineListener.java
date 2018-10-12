/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.listeners.velocity;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.PlayerPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.player.BungeePlayerRegisterProcessor;
import com.djrapitops.plan.system.processing.processors.player.IPUpdateProcessor;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.api.utility.log.Log;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Player Join listener for Velocity.
 *
 * Based on the bungee version.
 *
 * @author MicleBrick
 */
public class PlayerOnlineListener {

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String name = player.getUsername();
            InetAddress address = player.getRemoteAddress().getAddress();
            long now = System.currentTimeMillis();

            SessionCache.getInstance().cacheSession(uuid, new Session(uuid, now, "", ""));

            // maybe rename to ProxyPlayerRegisterProcessor?
            Processing.submit(new BungeePlayerRegisterProcessor(uuid, name, now,
                    new IPUpdateProcessor(uuid, address, now))
            );
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(ServerInfo.getServerUUID()));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @Subscribe
    public void onLogout(DisconnectEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            SessionCache.getInstance().endSession(uuid, System.currentTimeMillis());
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(ServerInfo.getServerUUID()));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            long now = System.currentTimeMillis();
            // Replaces the current session in the cache.
            SessionCache.getInstance().cacheSession(uuid, new Session(uuid, now, "", ""));
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}
