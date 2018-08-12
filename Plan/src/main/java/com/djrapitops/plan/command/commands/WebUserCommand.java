package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.commands.webuser.WebCheckCommand;
import com.djrapitops.plan.command.commands.webuser.WebDeleteCommand;
import com.djrapitops.plan.command.commands.webuser.WebLevelCommand;
import com.djrapitops.plan.command.commands.webuser.WebListUsersCommand;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
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
        super.setColorScheme(plugin.getColorScheme());

        Locale locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.WEB));
        setInDepthHelp(locale.getArray(DeepHelpLang.WEB));
        CommandNode[] webGroup = {
                register,
                new WebLevelCommand(plugin),
                new WebListUsersCommand(plugin),
                new WebCheckCommand(plugin),
                new WebDeleteCommand(plugin)
        };
        setNodeGroups(webGroup);
    }
}
