package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;

/**
 * Command used to display url to the network page.
 *
 * @author Rsl1122
 */
public class NetworkCommand extends SubCommand {

    public NetworkCommand() {
        super("network, n, netw",
                CommandType.CONSOLE,
                Permissions.ANALYZE.getPermission(),
                "Get the link to the network page");
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        sendNetworkMsg(sender);
        return true;
    }

    private void sendNetworkMsg(ISender sender) {
        sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = ConnectionSystem.getAddress() + "/network/";
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