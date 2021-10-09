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

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.registry.RegistryTypes;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for World change on Sponge.
 *
 * @author AuroraLS3
 */
public class SpongeWorldChangeListener {

    private final WorldAliasSettings worldAliasSettings;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public SpongeWorldChangeListener(
            WorldAliasSettings worldAliasSettings,
            ServerInfo serverInfo,
            DBSystem dbSystem,
            ErrorLogger errorLogger
    ) {
        this.worldAliasSettings = worldAliasSettings;
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;
    }

    @Listener(order = Order.POST)
    public void onWorldChange(ChangeEntityWorldEvent event, @First ServerPlayer player) {
        try {
            actOnEvent(event, player);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnEvent(ChangeEntityWorldEvent event, ServerPlayer player) {
        long time = System.currentTimeMillis();

        UUID uuid = player.uniqueId();

        String worldName = event.destinationWorld().key().asString(); // TODO(vankka): check that this is the right thing (also PlayerOnlineListener)
        String gameMode = getGameMode(player);

        dbSystem.getDatabase().executeTransaction(new WorldNameStoreTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

    private String getGameMode(ServerPlayer player) {
        GameMode gameMode = player.gameMode().get();
        return gameMode.key(RegistryTypes.GAME_MODE).asString().toUpperCase(); // TODO(vankka): check that this is the right thing (also PlayerOnlineListener)
    }

}
