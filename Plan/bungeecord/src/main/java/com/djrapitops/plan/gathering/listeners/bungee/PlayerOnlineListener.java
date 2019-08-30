/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.gathering.listeners.bungee;

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.webserver.cache.PageId;
import com.djrapitops.plan.delivery.webserver.cache.ResponseCache;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.gathering.cache.GeolocationCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.processing.processors.Processors;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.transactions.events.GeoInfoStoreTransaction;
import com.djrapitops.plan.system.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
    private final DBSystem dbSystem;
    private final ExtensionServiceImplementation extensionService;
    private final GeolocationCache geolocationCache;
    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processors processors,
            Processing processing,
            DBSystem dbSystem,
            ExtensionServiceImplementation extensionService,
            GeolocationCache geolocationCache,
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processors = processors;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.extensionService = extensionService;
        this.geolocationCache = geolocationCache;
        this.sessionCache = sessionCache;
        this.serverInfo = serverInfo;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostLogin(PostLoginEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            String playerName = player.getName();
            InetAddress address = player.getAddress().getAddress();
            long time = System.currentTimeMillis();

            Session session = new Session(playerUUID, serverInfo.getServerUUID(), time, null, null);
            session.putRawData(SessionKeys.SERVER_NAME, "Proxy Server");
            sessionCache.cacheSession(playerUUID, session);
            Database database = dbSystem.getDatabase();

            boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
            if (gatheringGeolocations) {
                database.executeTransaction(
                        new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
                );
            }

            database.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> time, playerName));
            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
            processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_JOIN));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID())); // TODO Swap to clearing data after creating JSON cache.
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void beforeLogout(PlayerDisconnectEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_LEAVE));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLogout(PlayerDisconnectEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            sessionCache.endSession(playerUUID, System.currentTimeMillis());
            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID())); // TODO Swap to clearing data after creating JSON cache.
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            long time = System.currentTimeMillis();
            // Replaces the current session in the cache.
            Session session = new Session(playerUUID, serverInfo.getServerUUID(), time, null, null);
            session.putRawData(SessionKeys.SERVER_NAME, "Proxy Server");
            sessionCache.cacheSession(playerUUID, session);
            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}
