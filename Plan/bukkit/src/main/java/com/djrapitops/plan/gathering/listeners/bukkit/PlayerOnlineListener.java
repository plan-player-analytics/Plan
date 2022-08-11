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

import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.BukkitPlayerData;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.BanStatusTransaction;
import com.djrapitops.plan.storage.database.transactions.events.KickStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreSessionTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author AuroraLS3
 */
public class PlayerOnlineListener implements Listener {

    private final PlayerJoinEventConsumer playerJoinEventConsumer;
    private final JoinAddressCache joinAddressCache;

    private final PlanConfig config;
    private final Processing processing;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;
    private final ErrorLogger errorLogger;
    private final Status status;

    @Inject
    public PlayerOnlineListener(
            PlayerJoinEventConsumer playerJoinEventConsumer,
            JoinAddressCache joinAddressCache,

            PlanConfig config,
            Processing processing,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ExtensionSvc extensionService,
            Exporter exporter,
            NicknameCache nicknameCache,
            SessionCache sessionCache,
            Status status,
            ErrorLogger errorLogger
    ) {
        this.playerJoinEventConsumer = playerJoinEventConsumer;
        this.joinAddressCache = joinAddressCache;
        this.config = config;
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.extensionService = extensionService;
        this.exporter = exporter;
        this.nicknameCache = nicknameCache;
        this.sessionCache = sessionCache;
        this.status = status;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            PlayerLoginEvent.Result result = event.getResult();
            UUID playerUUID = event.getPlayer().getUniqueId();
            ServerUUID serverUUID = serverInfo.getServerUUID();
            boolean banned = result == PlayerLoginEvent.Result.KICK_BANNED;

            String joinAddress = event.getHostname();
            if (!joinAddress.isEmpty()) {
                joinAddress = joinAddress.substring(0, joinAddress.lastIndexOf(':'));
                joinAddressCache.put(playerUUID, joinAddress);
            }
            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, () -> banned));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event, event.getResult()).build());
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
            if (status.areKicksNotCounted() || event.isCancelled()) {
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            if (BukkitAFKListener.afkTracker.isAfk(uuid)) {
                return;
            }

            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(uuid));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnJoinEvent(PlayerJoinEvent event) {
        long time = System.currentTimeMillis();
        UUID playerUUID = event.getPlayer().getUniqueId();
        BukkitAFKListener.afkTracker.performedAction(playerUUID, time);

        playerJoinEventConsumer.onJoinGameServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new BukkitPlayerData(event.getPlayer(), joinAddressCache.getNullableString(playerUUID)))
                .time(time)
                .build());
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
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnQuitEvent(PlayerQuitEvent event) {
        long time = System.currentTimeMillis();
        Player player = event.getPlayer();
        String playerName = player.getName();
        UUID playerUUID = player.getUniqueId();
        ServerUUID serverUUID = serverInfo.getServerUUID();

        BukkitAFKListener.afkTracker.loggedOut(playerUUID, time);

        joinAddressCache.remove(playerUUID);
        nicknameCache.removeDisplayName(playerUUID);

        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, player::isBanned));

        sessionCache.endSession(playerUUID, time)
                .ifPresent(endedSession -> dbSystem.getDatabase().executeTransaction(new StoreSessionTransaction(endedSession)));

        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }
}
