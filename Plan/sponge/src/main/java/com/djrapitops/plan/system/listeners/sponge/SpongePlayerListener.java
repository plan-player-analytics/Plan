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
package com.djrapitops.plan.system.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.objects.Nickname;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.events.*;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.cache.NicknameCache;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DataGatheringSettings;
import com.djrapitops.plan.system.status.Status;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ProviderRegistration;
import org.spongepowered.api.service.ban.BanService;

import javax.inject.Inject;
import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for Player Join/Leave on Sponge.
 *
 * @author Rsl1122
 */
public class SpongePlayerListener {

    private final PlanConfig config;
    private final Processors processors;
    private final Processing processing;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final GeolocationCache geolocationCache;
    private final NicknameCache nicknameCache;
    private final SessionCache sessionCache;
    private final Status status;
    private final ErrorHandler errorHandler;

    @Inject
    public SpongePlayerListener(
            PlanConfig config,
            Processors processors,
            Processing processing,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            GeolocationCache geolocationCache,
            NicknameCache nicknameCache,
            SessionCache sessionCache,
            Status status,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processors = processors;
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.geolocationCache = geolocationCache;
        this.nicknameCache = nicknameCache;
        this.sessionCache = sessionCache;
        this.status = status;
        this.errorHandler = errorHandler;
    }

    @Listener(order = Order.POST)
    public void onLogin(ClientConnectionEvent.Login event) {
        try {
            actOnLoginEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnLoginEvent(ClientConnectionEvent.Login event) {
        GameProfile profile = event.getProfile();
        UUID playerUUID = profile.getUniqueId();
        boolean banned = isBanned(profile);
        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, () -> banned));
    }

    @Listener(order = Order.POST)
    public void onKick(KickPlayerEvent event) {
        try {
            UUID playerUUID = event.getTargetEntity().getUniqueId();
            if (!status.areKicksCounted() || SpongeAFKListener.AFK_TRACKER.isAfk(playerUUID)) {
                return;
            }
            dbSystem.getDatabase().executeTransaction(new KickStoreTransaction(playerUUID));
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private boolean isBanned(GameProfile profile) {
        Optional<ProviderRegistration<BanService>> banService = Sponge.getServiceManager().getRegistration(BanService.class);
        boolean banned = false;
        if (banService.isPresent()) {
            banned = banService.get().getProvider().isBanned(profile);
        }
        return banned;
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event) {
        try {
            actOnJoinEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnJoinEvent(ClientConnectionEvent.Join event) {
        Player player = event.getTargetEntity();

        UUID uuid = player.getUniqueId();
        UUID serverUUID = serverInfo.getServerUUID();
        long time = System.currentTimeMillis();

        SpongeAFKListener.AFK_TRACKER.performedAction(uuid, time);

        String world = player.getWorld().getName();
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        String gm = gameMode.map(mode -> mode.getName().toUpperCase()).orElse("ADVENTURE");

        Database database = dbSystem.getDatabase();
        database.executeTransaction(new WorldNameStoreTransaction(serverUUID, world));

        InetAddress address = player.getConnection().getAddress().getAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayNameData().displayName().get().toPlain();

        boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
        if (gatheringGeolocations) {
            database.executeTransaction(
                    new GeoInfoStoreTransaction(uuid, address, time, geolocationCache::getCountry)
            );
        }

        database.executeTransaction(new PlayerServerRegisterTransaction(uuid, () -> time, playerName, serverUUID));
        sessionCache.cacheSession(uuid, new Session(uuid, serverUUID, time, world, gm))
                .ifPresent(previousSession -> database.executeTransaction(new SessionEndTransaction(previousSession)));

        database.executeTransaction(new NicknameStoreTransaction(
                uuid, new Nickname(displayName, time, serverUUID),
                (playerUUID, name) -> name.equals(nicknameCache.getDisplayName(playerUUID))
        ));

        processing.submitNonCritical(processors.info().playerPageUpdateProcessor(uuid));
    }

    @Listener(order = Order.POST)
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        try {
            actOnQuitEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnQuitEvent(ClientConnectionEvent.Disconnect event) {
        long time = System.currentTimeMillis();
        Player player = event.getTargetEntity();
        UUID playerUUID = player.getUniqueId();

        SpongeAFKListener.AFK_TRACKER.loggedOut(playerUUID, time);

        nicknameCache.removeDisplayName(playerUUID);

        boolean banned = isBanned(player.getProfile());
        dbSystem.getDatabase().executeTransaction(new BanStatusTransaction(playerUUID, () -> banned));

        sessionCache.endSession(playerUUID, time)
                .ifPresent(endedSession -> dbSystem.getDatabase().executeTransaction(new SessionEndTransaction(endedSession)));

        processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
    }
}