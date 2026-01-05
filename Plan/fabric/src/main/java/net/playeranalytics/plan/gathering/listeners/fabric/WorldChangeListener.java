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
package net.playeranalytics.plan.gathering.listeners.fabric;

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreWorldNameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class WorldChangeListener implements FabricListener {

    private final WorldAliasSettings worldAliasSettings;
    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;

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

    public void onWorldChange(ServerPlayerEntity player) {
        try {
            actOnEvent(player);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(getClass(), player).build());
        }
    }

    private void actOnEvent(ServerPlayerEntity player) {
        long time = System.currentTimeMillis();

        UUID uuid = player.getUuid();

        String worldName = player.getEntityWorld().getRegistryKey().getValue().toString();
        String gameMode = player.interactionManager.getGameMode().name();

        dbSystem.getDatabase().executeTransaction(new StoreWorldNameTransaction(serverInfo.getServerUUID(), worldName));
        worldAliasSettings.addWorld(worldName);

        Optional<ActiveSession> cachedSession = SessionCache.getCachedSession(uuid);
        cachedSession.ifPresent(session -> session.changeState(worldName, gameMode, time));
    }

    @Override
    public void register() {
        if (this.wasRegistered) {
            return;
        }

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            if (!this.isEnabled) {
                return;
            }
            onWorldChange(player);
        });

        this.enable();
        this.wasRegistered = true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

    @Override
    public void enable() {
        this.isEnabled = true;
    }

    @Override
    public void disable() {
        this.isEnabled = false;
    }
}
