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
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.api.exceptions.connection.ConnectionFailException;
import com.djrapitops.plan.api.exceptions.connection.NoServersException;
import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.UnauthorizedServerException;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.command.Sender;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Sends a request to cache players inspect page to the ResponseCache on the appropriate WebServer.
 *
 * @author Rsl1122
 */
public class InspectCacheRequestProcessor implements Runnable {

    private final UUID uuid;
    private final Sender sender;
    private final String playerName;
    private final BiConsumer<Sender, String> msgSender;

    private final InfoSystem infoSystem;
    private final WebExceptionLogger webExceptionLogger;

    InspectCacheRequestProcessor(
            UUID uuid,
            Sender sender,
            String playerName,
            BiConsumer<Sender, String> msgSender,
            InfoSystem infoSystem,
            WebExceptionLogger webExceptionLogger
    ) {
        this.uuid = uuid;
        this.sender = sender;
        this.playerName = playerName;
        this.msgSender = msgSender;
        this.infoSystem = infoSystem;
        this.webExceptionLogger = webExceptionLogger;
    }

    @Override
    public void run() {
        SessionCache.refreshActiveSessionsState();
        webExceptionLogger.logIfOccurs(this.getClass(), () -> {
            try {
                infoSystem.generateAndCachePlayerPage(uuid);
                msgSender.accept(sender, playerName);
            } catch (ConnectionFailException | UnauthorizedServerException
                    | NotFoundException | NoServersException e) {
                sender.sendMessage("§c" + e.getMessage());
            }
        });
    }
}
