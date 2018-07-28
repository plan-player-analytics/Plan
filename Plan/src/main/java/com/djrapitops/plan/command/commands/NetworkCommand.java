package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;

/**
 * Command used to display url to the network page.
 *
 * @author Rsl1122
 */
public class NetworkCommand extends CommandNode {

    private final Locale locale;

    public NetworkCommand(PlanPlugin plugin) {
        super("network|n|netw", Permissions.ANALYZE.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.NETWORK));
        setInDepthHelp(locale.getArray(DeepHelpLang.NETWORK));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sendNetworkMsg(sender);
    }

    private void sendNetworkMsg(ISender sender) {
        sender.sendMessage(locale.get(Msg.CMD_CONSTANT_FOOTER).parse());

        // Link
        String url = ConnectionSystem.getAddress() + "/network/";
        String message = locale.get(Msg.CMD_INFO_LINK).toString();
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sender.sendLink("   ", locale.get(Msg.CMD_INFO_CLICK_ME).toString(), url);
        }
        sender.sendMessage(locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
    }
}