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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.StoreNicknameTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Event Listener for chat events.
 *
 * @author AuroraLS3
 */
@Singleton
public class ChatListener implements FabricListener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final NicknameCache nicknameCache;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;
    private boolean wasRegistered = false;


    @Inject
    public ChatListener(
            ServerInfo serverInfo,
            DBSystem dbSystem,
            NicknameCache nicknameCache,
            ErrorLogger errorLogger
    ) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.nicknameCache = nicknameCache;
        this.errorLogger = errorLogger;
    }

    public void onChat(ServerPlayerEntity player) {
        try {
            actOnChatEvent(player);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(player).build());
        }
    }

    private void actOnChatEvent(ServerPlayerEntity player) {
        long time = System.currentTimeMillis();
        UUID uuid = player.getUuid();
        String displayName = player.getDisplayName().getString();

        dbSystem.getDatabase().executeTransaction(new StoreNicknameTransaction(
                uuid, new Nickname(displayName, time, serverInfo.getServerUUID()),
                (playerUUID, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));
    }

    @Override
    public void register() {
        if (this.wasRegistered) {
            return;
        }

        ServerMessageEvents.CHAT_MESSAGE.register((message, sender, params) -> {
            if (!isEnabled) {
                return;
            }
            onChat(sender);
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
