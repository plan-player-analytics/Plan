package net.playeranalytics.plan.gathering.listeners.fabric;

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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.NicknameStoreTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.playeranalytics.plan.gathering.listeners.FabricListener;
import net.playeranalytics.plan.gathering.listeners.events.PlanFabricEvents;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for chat events.
 *
 * @author AuroraLS3
 */
public class ChatListener implements FabricListener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final NicknameCache nicknameCache;
    private final ErrorLogger errorLogger;

    private boolean isEnabled = false;


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

    public void onChat(ServerPlayNetworkHandler handler, String message) {

        try {
            actOnChatEvent(handler);
        } catch (Exception e) {
            errorLogger.error(e, ErrorContext.builder().related(handler, message).build());
        }
    }

    private void actOnChatEvent(ServerPlayNetworkHandler handler) {
        long time = System.currentTimeMillis();
        ServerPlayerEntity player = handler.player;
        UUID uuid = player.getUuid();
        String displayName = player.getDisplayName().asString();

        dbSystem.getDatabase().executeTransaction(new NicknameStoreTransaction(
                uuid, new Nickname(displayName, time, serverInfo.getServerUUID()),
                (playerUUID, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));
    }

    @Override
    public void register() {
        PlanFabricEvents.ON_CHAT.register((handler, message) -> {
            if (!isEnabled) {
                return;
            }
            onChat(handler, message);

        });

        this.enable();
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

