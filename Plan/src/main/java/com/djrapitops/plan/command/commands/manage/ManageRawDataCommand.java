package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.info.connection.ConnectionSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * This manage subcommand is used to remove a single player's data from the
 * database.
 *
 * @author Rsl1122
 */
public class ManageRawDataCommand extends CommandNode {

    private final Locale locale;
    private final ConnectionSystem connectionSystem;

    @Inject
    public ManageRawDataCommand(Locale locale, ConnectionSystem connectionSystem) {
        super("raw", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.connectionSystem = connectionSystem;

        setArguments("<player>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_RAW_DATA));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_RAW_DATA));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        sender.sendMessage(locale.getString(CommandLang.HEADER_INSPECT, playerName));
        // Link
        String url = connectionSystem.getMainAddress() + "/player/" + playerName + "/raw";
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
