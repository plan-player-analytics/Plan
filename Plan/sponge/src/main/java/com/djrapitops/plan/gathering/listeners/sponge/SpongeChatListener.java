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

import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.NicknameStoreTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.MessageChannelEvent;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Listener that keeps track of player display name.
 *
 * @author AuroraLS3
 */
public class SpongeChatListener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final NicknameCache nicknameCache;
    private final ErrorLogger errorLogger;

    @Inject
    public SpongeChatListener(
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

    @Listener(order = Order.POST)
    public void onPlayerChat(MessageChannelEvent.Chat event, @First Player player) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnChatEvent(player);
        } catch (Exception e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder().related(event).build());
        }
    }

    private void actOnChatEvent(@First Player player) {
        long time = System.currentTimeMillis();
        UUID uuid = player.getUniqueId();
        String displayName = player.getDisplayNameData().displayName().get().toPlain();

        dbSystem.getDatabase().executeTransaction(new NicknameStoreTransaction(
                uuid, new Nickname(displayName, time, serverInfo.getServerUUID()),
                (playerUUID, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));
    }
}