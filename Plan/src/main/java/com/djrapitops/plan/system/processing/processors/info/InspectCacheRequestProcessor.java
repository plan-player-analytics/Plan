/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.processing.processors.info;

import com.djrapitops.plan.api.exceptions.connection.*;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.info.InfoSystem;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.processing.processors.player.PlayerProcessor;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

import java.util.UUID;

/**
 * Sends a request to cache players inspect page to the ResponseCache on the appropriate WebServer.
 *
 * @author Rsl1122
 */
public class InspectCacheRequestProcessor extends PlayerProcessor {

    private final ISender sender;
    private final String playerName;

    public InspectCacheRequestProcessor(UUID uuid, ISender sender, String playerName) {
        super(uuid);
        this.playerName = playerName;
        this.sender = sender;
    }

    @Override
    public void process() {
        SessionCache.refreshActiveSessionsState();
        try {
            InfoSystem.getInstance().generateAndCachePlayerPage(getUUID());
            sendInspectMsg(sender, playerName);
        } catch (ConnectionFailException | UnsupportedTransferDatabaseException | UnauthorizedServerException
                | NotFoundException | NoServersException e) {
            // TODO Test if this is appropriate
            sender.sendMessage("§c" + e.getMessage());
        } catch (WebException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void sendInspectMsg(ISender sender, String playerName) {
        sender.sendMessage(Locale.get(Msg.CMD_HEADER_INSPECT) + " " + playerName);
        // Link
        String url = ConnectionSystem.getInstance().getMainAddress() + "/player/" + playerName;
        String message = Locale.get(Msg.CMD_INFO_LINK).toString();
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sender.sendLink("   ", Locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
        }

        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }
}