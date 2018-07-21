package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

/**
 * Command used to display url to the player list page.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ListCommand extends CommandNode {

    public ListCommand() {
        super("players|pl|playerlist|list", Permissions.INSPECT_OTHER.getPermission(), CommandType.CONSOLE);
        setShortHelp(Locale.get(Msg.CMD_USG_LIST).toString());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_LIST).toArray());
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sendListMsg(sender);
    }

    private void sendListMsg(ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = ConnectionSystem.getAddress() + "/players/";
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