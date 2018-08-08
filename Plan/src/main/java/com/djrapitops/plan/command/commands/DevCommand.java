/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

/**
 * Command used for testing functions that are too difficult to unit test.
 *
 * @author Rsl1122
 */
public class DevCommand extends CommandNode {

    private final Locale locale;

    public DevCommand(PlanPlugin plugin) {
        super("dev", "plan.*", CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.get(CmdHelpLang.DEV).toString());
        setArguments("<feature>");
    }

    @Override
    public void onCommand(ISender sender, String cmd, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        sender.sendMessage("No features currently implemented in the command.");
    }
}
