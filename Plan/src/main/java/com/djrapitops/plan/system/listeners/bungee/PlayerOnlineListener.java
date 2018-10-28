/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the LGNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  LGNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.listeners.bungee;

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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
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

    private final PlanConfig config;
    private final Processors processors;
    private final Processing processing;
    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processors processors,
            Processing processing,
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processors = processors;
        this.processing = processing;
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

    @EventHandler
    public void onLogout(PlayerDisconnectEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID uuid = player.getUniqueId();

            sessionCache.endSession(uuid, System.currentTimeMillis());
            processing.submit(processors.info().playerPageUpdateProcessor(uuid));
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

            long time = System.currentTimeMillis();
            // Replaces the current session in the cache.
            sessionCache.cacheSession(uuid, new Session(uuid, serverInfo.getServerUUID(), time, "", ""));
            processing.submit(processors.info().playerPageUpdateProcessor(uuid));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}
