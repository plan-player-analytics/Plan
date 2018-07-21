/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

/**
 * Command used for testing functions that are too difficult to unit test.
 *
 * @author Rsl1122
 */
public class DevCommand extends CommandNode {

    public DevCommand() {
        super("dev", "plan.*", CommandType.PLAYER_OR_ARGS);
        setShortHelp("Test Plugin functions not testable with unit tests.");
        setArguments("<feature>");
    }

    @Override
    public void onCommand(ISender sender, String cmd, String[] args) {
        Verify.isTrue(args.length >= 1, () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString()));

        sender.sendMessage("No features currently implemented in the command.");
    }
}
