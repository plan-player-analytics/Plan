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
package com.djrapitops.plan.system.listeners.sponge;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.RunnableFactory;
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
    private SessionCache sessionCache;
    private RunnableFactory runnableFactory;
    private ErrorHandler errorHandler;

    @Inject
    public SpongePlayerListener(
            PlanConfig config,
            Processors processors,
            Processing processing,
            ServerInfo serverInfo,
            SessionCache sessionCache,
            RunnableFactory runnableFactory,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processors = processors;
        this.processing = processing;
        this.serverInfo = serverInfo;
        this.sessionCache = sessionCache;
        this.runnableFactory = runnableFactory;
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
        UUID uuid = profile.getUniqueId();
        boolean banned = isBanned(profile);
        processing.submit(processors.player().banAndOpProcessor(uuid, () -> banned, false));
    }

    @Listener(order = Order.POST)
    public void onKick(KickPlayerEvent event) {
        try {
            UUID uuid = event.getTargetEntity().getUniqueId();
            if (SpongeAFKListener.AFK_TRACKER.isAfk(uuid)) {
                return;
            }
            processing.submit(processors.player().kickProcessor(uuid));
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
        long time = System.currentTimeMillis();

        SpongeAFKListener.AFK_TRACKER.performedAction(uuid, time);

        String world = player.getWorld().getName();
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        String gm = gameMode.map(mode -> mode.getName().toUpperCase()).orElse("ADVENTURE");

        InetAddress address = player.getConnection().getAddress().getAddress();

        String playerName = player.getName();
        String displayName = player.getDisplayNameData().displayName().get().toPlain();

        boolean gatheringGeolocations = config.isTrue(Settings.DATA_GEOLOCATIONS);

        processing.submitCritical(() -> sessionCache.cacheSession(uuid, new Session(uuid, serverInfo.getServerUUID(), time, world, gm)));
        runnableFactory.create("Player Register: " + uuid,
                processors.player().registerProcessor(uuid, () -> time, playerName,
                        gatheringGeolocations ? processors.player().ipUpdateProcessor(uuid, address, time) : null,
                        processors.player().nameProcessor(uuid, playerName, displayName),
                        processors.info().playerPageUpdateProcessor(uuid)
                )
        ).runTaskAsynchronously();
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
        UUID uuid = player.getUniqueId();

        SpongeAFKListener.AFK_TRACKER.loggedOut(uuid, time);

        boolean banned = isBanned(player.getProfile());
        processing.submit(processors.player().banAndOpProcessor(uuid, () -> banned, false));
        processing.submit(processors.player().endSessionProcessor(uuid, time));
        processing.submit(processors.info().playerPageUpdateProcessor(uuid));
    }
}