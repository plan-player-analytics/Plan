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
package com.djrapitops.plan.gathering.listeners.sponge;

import com.djrapitops.plan.gathering.domain.SpongePlayerData;
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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanService;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for Player Join/Leave on Sponge.
 *
 * @author AuroraLS3
 */
public class PlayerOnlineListener {

    private final PlayerJoinEventConsumer joinEventConsumer;
    private final PlayerLeaveEventConsumer leaveEventConsumer;

    private final Game game;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final Status status;
    private final ErrorLogger errorLogger;

    @Inject
    public PlayerOnlineListener(
            PlayerJoinEventConsumer joinEventConsumer,
            PlayerLeaveEventConsumer leaveEventConsumer,
            Game game, ServerInfo serverInfo,
            DBSystem dbSystem,
            Status status,
            ErrorLogger errorLogger
    ) {
        this.joinEventConsumer = joinEventConsumer;
        this.leaveEventConsumer = leaveEventConsumer;
        this.game = game;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.status = status;
        this.errorLogger = errorLogger;
    }

    @Listener(order = Order.POST)
    public void onLogin(ServerSideConnectionEvent.Login event) {
        try {
            actOnLoginEvent(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnLoginEvent(ServerSideConnectionEvent.Login event) {
        GameProfile profile = event.profile();
        UUID playerUUID = profile.uniqueId();
        ServerUUID serverUUID = serverInfo.getServerUUID();
        if (game.server().isWhitelistEnabled()) {
            game.server().serviceProvider().whitelistService().isWhitelisted(profile)
                    .thenAccept(whitelisted -> {
                        if (Boolean.FALSE.equals(whitelisted)) {
                            dbSystem.getDatabase().executeTransaction(new StoreAllowlistBounceTransaction(
                                    playerUUID,
                                    event.profile().name().orElse(event.user().uniqueId().toString()),
                                    serverUUID,
                                    System.currentTimeMillis()));
                        }
                    });
        }
        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, () -> isBanned(profile)));
    }

    @Listener(order = Order.POST)
    public void onKick(KickPlayerEvent event) {
        try {
            UUID playerUUID = event.player().uniqueId();
            if (status.areKicksNotCounted() || SpongeAFKListener.afkTracker.isAfk(playerUUID)) {
                return;
            }
            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(playerUUID));
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private boolean isBanned(GameProfile profile) {
        BanService banService = Sponge.server().serviceProvider().banService();
        Optional<Ban.Profile> ban = banService.find(profile).join();
        return ban.isPresent();
    }

    @Listener(order = Order.POST)
    public void onJoin(ServerSideConnectionEvent.Join event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnJoinEvent(ServerSideConnectionEvent.Join event) {
        long time = System.currentTimeMillis();
        ServerPlayer player = event.player();
        UUID playerUUID = player.uniqueId();
        SpongeAFKListener.afkTracker.performedAction(playerUUID, time);

        joinEventConsumer.onJoinGameServer(PlayerJoin.builder()
                .server(serverInfo.getServer())
                .player(new SpongePlayerData(player))
                .time(time)
                .build());
    }

    @Listener(order = Order.DEFAULT)
    public void beforeQuit(ServerSideConnectionEvent.Disconnect event) {
        getPlayer(event)
                .ifPresent(player -> leaveEventConsumer.beforeLeave(PlayerLeave.builder()
                        .server(serverInfo.getServer())
                        .player(player)
                        .time(System.currentTimeMillis())
                        .build()));
    }

    @Listener(order = Order.POST)
    public void onQuit(ServerSideConnectionEvent.Disconnect event) {
        try {
            actOnQuitEvent(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnQuitEvent(ServerSideConnectionEvent.Disconnect event) {
        long time = System.currentTimeMillis();
        getPlayer(event)
                .ifPresent(player -> {
                    UUID playerUUID = player.getUUID();
                    SpongeAFKListener.afkTracker.loggedOut(playerUUID, time);
                    leaveEventConsumer.onLeaveGameServer(PlayerLeave.builder()
                            .server(serverInfo.getServer())
                            .player(player)
                            .time(System.currentTimeMillis())
                            .build());
                });
    }

    private @NotNull Optional<SpongePlayerData> getPlayer(ServerSideConnectionEvent.Disconnect event) {
        return event.profile()
                .map(GameProfile::uuid)
                .flatMap(playerUUID -> game.server().player(playerUUID))
                .map(SpongePlayerData::new);
    }
}
