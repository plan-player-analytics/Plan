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
 * Command used to display url to the player list page.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ListPlayersCommand extends CommandNode {

    private final Locale locale;

    public ListPlayersCommand(PlanPlugin plugin) {
        super("players|pl|playerlist|list", Permissions.INSPECT_OTHER.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.PLAYERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAYERS));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        sendListMsg(sender);
    }

    private void sendListMsg(ISender sender) {
        sender.sendMessage(locale.getString(Msg.CMD_CONSTANT_FOOTER));

        // Link
        String url = ConnectionSystem.getAddress() + "/players/";
        String message = locale.getString(Msg.CMD_INFO_LINK);
        boolean console = !CommandUtils.isPlayer(sender);
        if (console) {
            sender.sendMessage(message + url);
        } else {
            sender.sendMessage(message);
            sender.sendLink("   ", locale.getString(Msg.CMD_INFO_CLICK_ME), url);
        }
        sender.sendMessage(locale.getString(Msg.CMD_CONSTANT_FOOTER));
    }
}