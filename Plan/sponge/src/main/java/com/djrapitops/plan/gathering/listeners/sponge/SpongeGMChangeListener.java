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
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.WorldNameStoreTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.living.humanoid.ChangeGameModeEvent;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for GameMode change on Sponge.
 *
 * @author AuroraLS3
 */
public class SpongeGMChangeListener {

    private final WorldAliasSettings worldAliasSettings;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public SpongeGMChangeListener(
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
    public void onGMChange(ChangeGameModeEvent.TargetPlayer event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnGMChangeEvent(event);
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(event, event.getGameMode()).build());
        }
    }

    private void actOnGMChangeEvent(ChangeGameModeEvent.TargetPlayer event) {
        Player player = event.getTargetEntity();
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();

        String gameMode = event.getGameMode().getName().toUpperCase();
        String worldName = player.getWorld().getName();

        dbSystem.getDatabase().executeTransaction(new WorldNameStoreTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

}