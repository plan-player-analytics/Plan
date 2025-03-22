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
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.data.ChangeDataHolderEvent;
import org.spongepowered.api.registry.RegistryTypes;

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
    public void onGMChange(ChangeDataHolderEvent.ValueChange event) {
        ServerPlayer player = event.targetHolder() instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        if (player == null) {
            return;
        }

        DataTransactionResult result = event.endResult();
        Optional<Value.Immutable<GameMode>> gameModeValue = result.successfulValue(Keys.GAME_MODE);
        if (gameModeValue.isEmpty()) {
            return;
        }

        GameMode newMode = gameModeValue.get().get();
        actOnGMChangeEvent(player, newMode);
    }

    private void actOnGMChangeEvent(ServerPlayer player, GameMode gameMode) {
        UUID uuid = player.uniqueId();
        long time = System.currentTimeMillis();

        String gameModeText = gameMode.key(RegistryTypes.GAME_MODE).value().toUpperCase();
        String worldName = Optional.ofNullable(Sponge.game().server().worldManager().worldDirectory(player.world().key()))
                .map(path -> path.getFileName().toString()).orElse("Unknown");

        dbSystem.getDatabase().executeTransaction(new StoreWorldNameTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameModeText, time));
    }
}
