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
package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
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
    private final ErrorHandler errorHandler;

    @Inject
    public WorldChangeListener(
            WorldAliasSettings worldAliasSettings,
            ErrorHandler errorHandler
    ) {
        this.worldAliasSettings = worldAliasSettings;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        try {
            actOnEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnEvent(PlayerChangedWorldEvent event) {
        long time = System.currentTimeMillis();

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        String worldName = player.getWorld().getName();
        String gameMode = player.getGameMode().name();

        worldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }
}
