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

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

public class WorldChangeListener implements Listener {

    private final WorldAliasSettings worldAliasSettings;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public WorldChangeListener(
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        try {
            actOnEvent(event);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnEvent(PlayerChangedWorldEvent event) {
        long time = System.currentTimeMillis();

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String worldName = player.getWorld().getName();
        String gameMode = player.getGameMode().name();

        dbSystem.getDatabase().executeTransaction(new StoreWorldNameTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }
}
