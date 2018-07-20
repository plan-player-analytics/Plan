package com.djrapitops.plan.common.command.commands;

import com.djrapitops.plan.common.PlanPlugin;
import com.djrapitops.plan.common.command.commands.webuser.WebCheckCommand;
import com.djrapitops.plan.common.command.commands.webuser.WebDeleteCommand;
import com.djrapitops.plan.common.command.commands.webuser.WebLevelCommand;
import com.djrapitops.plan.common.command.commands.webuser.WebListUsersCommand;
import com.djrapitops.plan.common.system.settings.Permissions;
import com.djrapitops.plan.common.system.settings.locale.Locale;
import com.djrapitops.plan.common.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;

/**
 * Web subcommand used to manage Web users.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserCommand extends TreeCmdNode {

    public WebUserCommand(PlanPlugin plugin, RegisterCommand register, CommandNode parent) {
        super("webuser|web", Permissions.MANAGE_WEB.getPerm(), CommandType.CONSOLE, parent);
        setShortHelp(Locale.get(Msg.CMD_USG_WEB).toString());
        super.setColorScheme(plugin.getColorScheme());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_WEB).toArray());
        setNodeGroups(
                new CommandNode[]{
                        register,
                        new WebLevelCommand(plugin),
                        new WebListUsersCommand(plugin),
                        new WebCheckCommand(),
                        new WebDeleteCommand()
                }
        );
    }
}
