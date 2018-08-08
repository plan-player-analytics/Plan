/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

import java.util.UUID;

/**
 * Sends a request to cache players inspect page to the ResponseCache on the appropriate WebServer.
 *
 * @author Rsl1122
 */
public class InspectCacheRequestProcessor implements Runnable {

    private final UUID uuid;
    private final ISender sender;
    private final String playerName;
    private final Locale locale;

    public InspectCacheRequestProcessor(UUID uuid, ISender sender, String playerName, Locale locale) {
        this.uuid = uuid;
        this.sender = sender;
        this.playerName = playerName;
        this.locale = locale;
    }

    @Override
    public void run() {
        SessionCache.refreshActiveSessionsState();
        try {
            InfoSystem.getInstance().generateAndCachePlayerPage(uuid);
            sendInspectMsg(sender, playerName);
        } catch (ConnectionFailException | UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            sender.sendMessage("§c" + e.getMessage());
        } catch (WebException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    // TODO Move to InspectCommand somehow.
    private void sendInspectMsg(ISender sender, String playerName) {
        sender.sendMessage(locale.getString(CommandLang.HEADER_INSPECT, playerName));
        // Link
        String url = ConnectionSystem.getInstance().getMainAddress() + "/player/" + playerName;
        String linkPrefix = locale.getString(CommandLang.LINK_PREFIX);
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(linkPrefix + url);
        } else {
            sender.sendMessage(linkPrefix);
            sender.sendLink("   ", locale.getString(CommandLang.LINK_CLICK_ME), url);
        }

        sender.sendMessage(">");
    }
}
