/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.listeners.bungee;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.info.PlayerPageUpdateProcessor;
import com.djrapitops.plan.system.processing.processors.player.BungeePlayerRegisterProcessor;
import com.djrapitops.plan.system.processing.processors.player.IPUpdateProcessor;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.inject.Inject;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Player Join listener for Bungee.
 *
 * @author Rsl1122
 */
public class PlayerOnlineListener implements Listener {

    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public PlayerOnlineListener(
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorHandler errorHandler
    ) {
        this.sessionCache = sessionCache;
        this.serverInfo = serverInfo;
        this.errorHandler = errorHandler;
    }

    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            InetAddress address = player.getAddress().getAddress();
            long now = System.currentTimeMillis();

            sessionCache.cacheSession(uuid, new Session(uuid, now, "", ""));

            Processing.submit(new BungeePlayerRegisterProcessor(uuid, name, now,
                    new IPUpdateProcessor(uuid, address, now))
            );
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @EventHandler
    public void onLogout(ServerDisconnectEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            sessionCache.endSession(uuid, System.currentTimeMillis());
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @EventHandler
    public void onServerSwitch(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            long now = System.currentTimeMillis();
            // Replaces the current session in the cache.
            sessionCache.cacheSession(uuid, new Session(uuid, now, "", ""));
            Processing.submit(new PlayerPageUpdateProcessor(uuid));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}
