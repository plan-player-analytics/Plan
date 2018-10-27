package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;

import javax.inject.Inject;

/**
 * Command used to display url to the player list page.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class ListPlayersCommand extends CommandNode {

    private final Locale locale;
    private final ConnectionSystem connectionSystem;

    @Inject
    public ListPlayersCommand(Locale locale, ConnectionSystem connectionSystem) {
        super("players|pl|playerlist|list", Permissions.INSPECT_OTHER.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.connectionSystem = connectionSystem;

        setShortHelp(locale.getString(CmdHelpLang.PLAYERS));
        setInDepthHelp(locale.getArray(DeepHelpLang.PLAYERS));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        sendListMsg(sender);
    }

    private void sendListMsg(Sender sender) {
        sender.sendMessage(locale.getString(CommandLang.HEADER_PLAYERS));

        // Link
        String url = connectionSystem.getMainAddress() + "/players/";
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