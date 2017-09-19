/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.processing.info;

import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.processing.player.PlayerProcessor;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.html.HtmlUtils;

import java.util.UUID;

/**
 * Sends a request to cache players inspect page to the PageCache on the appropriate WebServer.
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
        MiscUtils.getIPlan().getInfoManager().cachePlayer(getUUID());
        sendInspectMsg(sender, playerName);
    }

    private void sendInspectMsg(ISender sender, String playerName) {
        sender.sendMessage(Locale.get(Msg.CMD_HEADER_INSPECT) + " " + playerName);
        // Link
        String url = HtmlUtils.getInspectUrlWithProtocol(playerName);
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