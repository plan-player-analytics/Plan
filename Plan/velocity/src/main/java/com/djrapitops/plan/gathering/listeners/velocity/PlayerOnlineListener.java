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
package com.djrapitops.plan.gathering.listeners.velocity;

import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.GeoInfoStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.velocitypowered.api.event.PostOrder;
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
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final GeolocationCache geolocationCache;
    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            DBSystem dbSystem,
            ExtensionSvc extensionService,
            Exporter exporter,
            GeolocationCache geolocationCache,
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.config = config;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.geolocationCache = geolocationCache;
        this.sessionCache = sessionCache;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;
    }

    @Subscribe(order = PostOrder.LAST)
    public void onPostLogin(PostLoginEvent event) {
        try {
            actOnLogin(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    public void actOnLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getUsername();
        InetAddress address = player.getRemoteAddress().getAddress();
        long time = System.currentTimeMillis();

        ActiveSession session = new ActiveSession(playerUUID, serverInfo.getServerUUID(), time, null, null);
        session.getExtraData().put(PlayerName.class, new PlayerName(playerName));
        session.getExtraData().put(ServerName.class, new ServerName("Proxy Server"));
        sessionCache.cacheSession(playerUUID, session);

        Database database = dbSystem.getDatabase();


        database.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> time, playerName))
                .thenRunAsync(() -> {
                    boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
                    if (gatheringGeolocations) {
                        database.executeTransaction(
                                new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
                        );
                    }

                    processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_JOIN));
                    if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
                        processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
                    }
                });
    }

    @Subscribe(order = PostOrder.NORMAL)
    public void beforeLogout(DisconnectEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getUsername();
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_LEAVE));
    }

    @Subscribe(order = PostOrder.LAST)
    public void onLogout(DisconnectEvent event) {
        try {
            actOnLogout(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    public void actOnLogout(DisconnectEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        UUID playerUUID = player.getUniqueId();

        sessionCache.endSession(playerUUID, System.currentTimeMillis());
        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }

    @Subscribe(order = PostOrder.LAST)
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            actOnServerSwitch(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    public void actOnServerSwitch(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        UUID playerUUID = player.getUniqueId();
        long time = System.currentTimeMillis();

        // Replaces the current session in the cache.
        ActiveSession session = new ActiveSession(playerUUID, serverInfo.getServerUUID(), time, null, null);
        session.getExtraData().put(PlayerName.class, new PlayerName(playerName));
        session.getExtraData().put(ServerName.class, new ServerName("Proxy Server"));
        sessionCache.cacheSession(playerUUID, session);

        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }
}
