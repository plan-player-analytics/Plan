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

import com.djrapitops.plan.gathering.JoinAddressValidator;
import com.djrapitops.plan.gathering.cache.JoinAddressCache;
import com.djrapitops.plan.gathering.domain.BukkitPlayerData;
import com.djrapitops.plan.gathering.domain.event.PlayerJoin;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;
import com.djrapitops.plan.gathering.events.PlayerJoinEventConsumer;
import com.djrapitops.plan.gathering.events.PlayerLeaveEventConsumer;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.BanStatusTransaction;
import com.djrapitops.plan.storage.database.transactions.events.KickStoreTransaction;
import com.djrapitops.plan.storage.database.transactions.events.StoreAllowlistBounceTransaction;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
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
    private final PlayerLeaveEventConsumer playerLeaveEventConsumer;
    private final JoinAddressValidator joinAddressValidator;
    private final JoinAddressCache joinAddressCache;

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;
    private final Status status;

    @Inject
    public PlayerOnlineListener(
            PlayerJoinEventConsumer playerJoinEventConsumer,
            PlayerLeaveEventConsumer playerLeaveEventConsumer,
            JoinAddressValidator joinAddressValidator,
            JoinAddressCache joinAddressCache,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            Status status,
            ErrorLogger errorLogger
    ) {
        this.playerJoinEventConsumer = playerJoinEventConsumer;
        this.playerLeaveEventConsumer = playerLeaveEventConsumer;
        this.joinAddressValidator = joinAddressValidator;
        this.joinAddressCache = joinAddressCache;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.status = status;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            UUID playerUUID = event.getPlayer().getUniqueId();
            ServerUUID serverUUID = serverInfo.getServerUUID();
            boolean banned = PlayerLoginEvent.Result.KICK_BANNED == event.getResult();
            boolean notWhitelisted = PlayerLoginEvent.Result.KICK_WHITELIST == event.getResult();

            if (notWhitelisted) {
                dbSystem.getDatabase().executeTransaction(new StoreAllowlistBounceTransaction(playerUUID, event.getPlayer().getName(), serverUUID, System.currentTimeMillis()));
            }

            @Untrusted String address = joinAddressValidator.sanitize(event.getHostname());
            if (joinAddressValidator.isValid(address)) {
                joinAddressCache.put(playerUUID, address);
            }
            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, banned));
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
        try {
            playerLeaveEventConsumer.beforeLeave(PlayerLeave.builder()
                    .server(serverInfo.getServer())
                    .player(new BukkitPlayerData(event.getPlayer(), null))
                    .time(System.currentTimeMillis())
                    .build());
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
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
        UUID playerUUID = event.getPlayer().getUniqueId();
        BukkitAFKListener.afkTracker.loggedOut(playerUUID, time);

        playerLeaveEventConsumer.onLeaveGameServer(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new BukkitPlayerData(event.getPlayer(), null))
                .time(System.currentTimeMillis())
                .build());
    }
}
