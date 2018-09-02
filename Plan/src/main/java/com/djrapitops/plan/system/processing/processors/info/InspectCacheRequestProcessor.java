/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.WebExceptionLogger;
import com.djrapitops.plugin.command.ISender;

import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Sends a request to cache players inspect page to the ResponseCache on the appropriate WebServer.
 *
 * @author Rsl1122
 */
public class InspectCacheRequestProcessor implements Runnable {

    private final UUID uuid;
    private final ISender sender;
    private final String playerName;
    private final BiConsumer<ISender, String> msgSender;

    private final InfoSystem infoSystem;
    private final WebExceptionLogger webExceptionLogger;

    InspectCacheRequestProcessor(
            UUID uuid,
            ISender sender,
            String playerName,
            BiConsumer<ISender, String> msgSender,
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
            } catch (ConnectionFailException | UnsupportedTransferDatabaseException | UnauthorizedServerException
                    | NotFoundException | NoServersException e) {
                sender.sendMessage("§c" + e.getMessage());
            }
        });
    }
}
