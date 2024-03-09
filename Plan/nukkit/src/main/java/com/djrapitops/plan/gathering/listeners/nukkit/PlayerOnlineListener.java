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
package com.djrapitops.plan.gathering.listeners.nukkit;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerKickEvent;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import com.djrapitops.plan.gathering.domain.NukkitPlayerData;
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
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for PlayerJoin, PlayerQuit and PlayerKickEvents.
 *
 * @author AuroraLS3
 */
public class PlayerOnlineListener implements Listener {

    private final PlayerJoinEventConsumer joinEventConsumer;
    private final PlayerLeaveEventConsumer leaveEventConsumer;

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;
    private final Status status;

    @Inject
    public PlayerOnlineListener(
            PlayerJoinEventConsumer joinEventConsumer, PlayerLeaveEventConsumer leaveEventConsumer,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            Status status,
            ErrorLogger errorLogger
    ) {
        this.joinEventConsumer = joinEventConsumer;
        this.leaveEventConsumer = leaveEventConsumer;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.status = status;
        this.errorLogger = errorLogger;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(PlayerLoginEvent event) {
        try {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            ServerUUID serverUUID = serverInfo.getServerUUID();
            dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, player::isBanned));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
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
            if (event.getReasonEnum() == PlayerKickEvent.Reason.NOT_WHITELISTED) {
                dbSystem.getDatabase().executeTransaction(new StoreAllowlistBounceTransaction(
                        event.getPlayer().getUniqueId(),
                        event.getPlayer().getName(),
                        serverInfo.getServerUUID(), System.currentTimeMillis())
                );
            }
            if (status.areKicksNotCounted() || event.isCancelled()) {
                return;
            }
            UUID uuid = event.getPlayer().getUniqueId();
            if (NukkitAFKListener.afkTracker.isAfk(uuid)) {
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
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        if (playerUUID == null) return; // Can be null when player is not signed in to xbox live

        NukkitAFKListener.afkTracker.performedAction(playerUUID, time);

        joinEventConsumer.onJoinGameServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new NukkitPlayerData(player))
                .time(time)
                .build());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void beforePlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer().getUniqueId() == null) return; // Can be null when player is not signed in to xbox live

        leaveEventConsumer.beforeLeave(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new NukkitPlayerData(event.getPlayer()))
                .time(System.currentTimeMillis())
                .build());
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
        UUID playerUUID = player.getUniqueId();
        if (playerUUID == null) return; // Can be null when player is not signed in to xbox live

        NukkitAFKListener.afkTracker.loggedOut(playerUUID, time);

        leaveEventConsumer.onLeaveGameServer(PlayerLeave.builder()
                .server(serverInfo.getServer())
                .player(new NukkitPlayerData(event.getPlayer()))
                .time(System.currentTimeMillis())
                .build());
    }
}
