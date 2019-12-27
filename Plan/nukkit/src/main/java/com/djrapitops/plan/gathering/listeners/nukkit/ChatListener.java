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
package com.djrapitops.plan.gathering.listeners.nukkit;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerChatEvent;
import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.gathering.cache.NicknameCache;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.NicknameStoreTransaction;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.UUID;

/**
 * Event Listener for AsyncPlayerChatEvents.
 *
 * @author Rsl1122
 */
public class ChatListener implements Listener {

    private final ServerInfo serverInfo;
    private final DBSystem dbSystem;
    private final NicknameCache nicknameCache;
    private final ErrorHandler errorHandler;

    @Inject
    public ChatListener(
            ServerInfo serverInfo,
            DBSystem dbSystem,
            NicknameCache nicknameCache,
            ErrorHandler errorHandler
    ) {
        this.serverInfo = serverInfo;
        this.dbSystem = dbSystem;
        this.nicknameCache = nicknameCache;
        this.errorHandler = errorHandler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        try {
            actOnChatEvent(event);
        } catch (Exception e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void actOnChatEvent(PlayerChatEvent event) {
        long time = System.currentTimeMillis();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        String displayName = player.getDisplayName();

        dbSystem.getDatabase().executeTransaction(new NicknameStoreTransaction(
                uuid, new Nickname(displayName, time, serverInfo.getServerUUID()),
                (playerUUID, name) -> nicknameCache.getDisplayName(playerUUID).map(name::equals).orElse(false)
        ));
    }
}
