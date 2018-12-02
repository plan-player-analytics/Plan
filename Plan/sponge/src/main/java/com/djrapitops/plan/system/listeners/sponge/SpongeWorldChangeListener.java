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
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.cause.First;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;

/**
 * Listener for World change on Sponge.
 *
 * @author Rsl1122
 */
public class SpongeWorldChangeListener {

    private final WorldAliasSettings worldAliasSettings;
    private ErrorHandler errorHandler;

    @Inject
    public SpongeWorldChangeListener(
            WorldAliasSettings worldAliasSettings,
            ErrorHandler errorHandler
    ) {
        this.worldAliasSettings = worldAliasSettings;
        this.errorHandler = errorHandler;
    }

    @Listener(order = Order.POST)
    public void onWorldChange(MoveEntityEvent.Teleport event, @First Player player) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnEvent(event, player);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnEvent(MoveEntityEvent.Teleport event, Player player) {
        long time = System.currentTimeMillis();

        UUID uuid = player.getUniqueId();

        String worldName = event.getToTransform().getExtent().getName();
        String gameMode = getGameMode(player);

        worldAliasSettings.addWorld(worldName);

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

    private String getGameMode(Player player) {
        Optional<GameMode> gameMode = player.getGameModeData().get(Keys.GAME_MODE);
        return gameMode.map(gm -> gm.getName().toUpperCase()).orElse("ADVENTURE");
    }

}