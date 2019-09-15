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
package com.djrapitops.plan.gathering.listeners.bukkit;

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.delivery.webserver.cache.DataID;
import com.djrapitops.plan.delivery.webserver.cache.JSONCache;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionServiceImplementation;
import com.djrapitops.plan.gathering.cache.GeolocationCache;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author Rsl1122
 */
public class PlayerOnlineListener implements Listener {

    private final PlanConfig config;
    private final Processing processing;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ExtensionServiceImplementation extensionService;
    private final Exporter exporter;
    private final GeolocationCache geolocationCache;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;
    private final ErrorHandler errorHandler;
    private final Status status;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ExtensionServiceImplementation extensionService,
            Exporter exporter,
            GeolocationCache geolocationCache,
            NicknameCache nicknameCache,
            SessionCache sessionCache,
            Status status,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.geolocationCache = geolocationCache;
        this.nicknameCache = nicknameCache;
        this.sessionCache = sessionCache;
        this.status = status;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            PlayerLoginEvent.Result result = event.getResult();
            UUID playerUUID = event.getPlayer().getUniqueId();
            boolean operator = event.getPlayer().isOp();
            boolean banned = result == PlayerLoginEvent.Result.KICK_BANNED;
            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, () -> banned));
            dbSystem.getDatabase().executeTransaction(new OperatorStatusTransaction(playerUUID, operator));
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    /**
     * PlayerKickEvent Listener.
     * <p>
     * Adds processing information to the ProcessingQueue.
     * After KickEvent, the QuitEvent is automatically called.
     *
     * @param event Fired event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerKick(PlayerKickEvent event) {
        try {
            if (!status.areKicksCounted() || event.isCancelled()) {
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            if (BukkitAFKListener.AFK_TRACKER.isAfk(uuid)) {
                return;
            }

            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(uuid));
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        UUID playerUUID = player.getUniqueId();
        UUID serverUUID = serverInfo.getServerUUID();
        long time = System.currentTimeMillis();
        JSONCache.invalidate(DataID.SERVER_OVERVIEW, serverUUID);
        JSONCache.invalidate(DataID.GRAPH_PERFORMANCE, serverUUID);

        BukkitAFKListener.AFK_TRACKER.performedAction(playerUUID, time);

        String world = player.getWorld().getName();
        String gm = player.getGameMode().name();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, world));

        InetAddress address = player.getAddress().getAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayName();

        boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
        if (gatheringGeolocations) {
            database.executeTransaction(
                    new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
            );
        }

        database.executeTransaction(new PlayerServerRegisterTransaction(playerUUID, player::getFirstPlayed, playerName, serverUUID));
        Session session = new Session(playerUUID, serverUUID, time, world, gm);
        session.putRawData(SessionKeys.NAME, playerName);
        session.putRawData(SessionKeys.SERVER_NAME, serverInfo.getServer().getIdentifiableName());
        sessionCache.cacheSession(playerUUID, session)
                .ifPresent(previousSession -> database.executeTransaction(new SessionEndTransaction(previousSession)));

        database.executeTransaction(new NicknameStoreTransaction(
                playerUUID, new Nickname(displayName, time, serverUUID),
                (uuid, name) -> name.equals(nicknameCache.getDisplayName(uuid))
        ));

        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_JOIN));
        if (config.get(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void beforePlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        String playerName = player.getName();
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_LEAVE));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        try {
            actOnQuitEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnQuitEvent(PlayerQuitEvent event) {
        long time = System.currentTimeMillis();
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();
        UUID serverUUID = serverInfo.getServerUUID();
        JSONCache.invalidate(DataID.SERVER_OVERVIEW, serverUUID);
        JSONCache.invalidate(DataID.GRAPH_PERFORMANCE, serverUUID);

        BukkitAFKListener.AFK_TRACKER.loggedOut(playerUUID, time);

        nicknameCache.removeDisplayName(playerUUID);

        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, player::isBanned));

        sessionCache.endSession(playerUUID, time)
                .ifPresent(endedSession -> dbSystem.getDatabase().executeTransaction(new SessionEndTransaction(endedSession)));

        if (config.get(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }
}
