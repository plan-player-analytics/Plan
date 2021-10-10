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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.delivery.domain.PlayerName;
import com.djrapitops.plan.delivery.domain.ServerName;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.ExtensionSvc;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.geolocation.GeolocationCache;
import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.*;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanService;

import javax.inject.Inject;
import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Listener for Player Join/Leave on Sponge.
 *
 * @author AuroraLS3
 */
public class PlayerOnlineListener {

    private final PlanConfig config;
    private final Processing processing;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ExtensionSvc extensionService;
    private final Exporter exporter;
    private final GeolocationCache geolocationCache;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;
    private final Status status;
    private final ErrorLogger errorLogger;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ExtensionSvc extensionService,
            Exporter exporter, GeolocationCache geolocationCache,
            NicknameCache nicknameCache,
            SessionCache sessionCache,
            Status status,
            ErrorLogger errorLogger
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
        ServerPlayer player = event.player();

        UUID playerUUID = player.uniqueId();
        ServerUUID serverUUID = serverInfo.getServerUUID();
        long time = System.currentTimeMillis();

        SpongeAFKListener.afkTracker.performedAction(playerUUID, time);

        String world = Sponge.game().server().worldManager().worldDirectory(player.world().key())
                .map(path -> path.getFileName().toString()).orElse("Unknown");
        GameMode gameMode = player.gameMode().get();
        String gm = gameMode.key(RegistryTypes.GAME_MODE).value().toUpperCase();

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, world));

        InetAddress address = player.connection().address().getAddress();
        Supplier<String> getHostName = () -> player.connection().virtualHost().getHostString();

        String playerName = player.name();
        String displayName = LegacyComponentSerializer.legacyAmpersand().serialize(player.displayName().get());

        boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
        if (gatheringGeolocations) {
            database.executeTransaction(
                    new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
            );
        }

        database.executeTransaction(new PlayerServerRegisterTransaction(playerUUID, () -> time,
                playerName, serverUUID, getHostName));
        ActiveSession session = new ActiveSession(playerUUID, serverUUID, time, world, gm);
        session.getExtraData().put(PlayerName.class, new PlayerName(playerName));
        session.getExtraData().put(ServerName.class, new ServerName(serverInfo.getServer().getIdentifiableName()));
        sessionCache.cacheSession(playerUUID, session)
                .ifPresent(previousSession -> database.executeTransaction(new SessionEndTransaction(previousSession)));

        database.executeTransaction(new NicknameStoreTransaction(
                playerUUID, new Nickname(displayName, time, serverUUID),
                (uuid, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));

        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_JOIN));
        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }

    @Listener(order = Order.DEFAULT)
    public void beforeQuit(ServerSideConnectionEvent.Disconnect event) {
        Player player = event.player();
        UUID playerUUID = player.uniqueId();
        String playerName = player.name();
        processing.submitNonCritical(() -> extensionService.updatePlayerValues(playerUUID, playerName, CallEvents.PLAYER_LEAVE));
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
        Player player = event.player();
        String playerName = player.name();
        UUID playerUUID = player.uniqueId();
        ServerUUID serverUUID = serverInfo.getServerUUID();

        SpongeAFKListener.afkTracker.loggedOut(playerUUID, time);

        nicknameCache.removeDisplayName(playerUUID);

        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, serverUUID, () -> isBanned(player.profile())));

        sessionCache.endSession(playerUUID, time)
                .ifPresent(endedSession -> dbSystem.getDatabase().executeTransaction(new SessionEndTransaction(endedSession)));

        if (config.isTrue(ExportSettings.EXPORT_ON_ONLINE_STATUS_CHANGE)) {
            processing.submitNonCritical(() -> exporter.exportPlayerPage(playerUUID, playerName));
        }
    }
}
