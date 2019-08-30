/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.gathering.listeners.Status;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * This manage SubCommand is used to disable some features of the plugin temporarily.
 *
 * @author Rsl1122
 */
public class ManageDisableCommand extends CommandNode {

    private final Locale locale;
    private final Status status;

    @Inject
    public ManageDisableCommand(
            Locale locale,
            Status status
    ) {
        super("disable", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.status = status;

        setArguments("<feature>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_DISABLE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_DISABLE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        if ("kickcount".equalsIgnoreCase(args[0])) {
            status.setCountKicks(false);
            sender.sendMessage(locale.getString(CommandLang.FEATURE_DISABLED, "Kick Counting"));
        } else {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_SUCH_FEATURE, "'kickcount'"));
        }
    }
}
