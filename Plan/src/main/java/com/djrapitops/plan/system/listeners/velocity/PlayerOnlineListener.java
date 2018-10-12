/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.listeners.velocity;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Player Join listener for Velocity.
 * <p>
 * Based on the bungee version.
 *
 * @author MicleBrick
 */
@Singleton
public class PlayerOnlineListener {

    private final PlanConfig config;
    private final Processors processors;
    private final Processing processing;
    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            Processors processors,
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processing = processing;
        this.processors = processors;
        this.sessionCache = sessionCache;
        this.serverInfo = serverInfo;
        this.errorHandler = errorHandler;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();
            String name = player.getUsername();
            InetAddress address = player.getRemoteAddress().getAddress();
            long time = System.currentTimeMillis();

            sessionCache.cacheSession(uuid, new Session(uuid, serverInfo.getServerUUID(), time, "", ""));

            boolean gatheringGeolocations = config.isTrue(Settings.DATA_GEOLOCATIONS);

            processing.submit(processors.player().proxyRegisterProcessor(uuid, name, time,
                    gatheringGeolocations ? processors.player().ipUpdateProcessor(uuid, address, time) : null
            ));
            processing.submit(processors.info().playerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @Subscribe
    public void onLogout(DisconnectEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            sessionCache.endSession(uuid, System.currentTimeMillis());
            processing.submit(processors.info().playerPageUpdateProcessor(uuid));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            Player player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            long now = System.currentTimeMillis();
            // Replaces the current session in the cache.
            sessionCache.cacheSession(uuid, new Session(uuid, serverInfo.getServerUUID(), now, "", ""));
            processing.submit(processors.info().playerPageUpdateProcessor(uuid));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}
