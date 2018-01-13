package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.commands.webuser.WebCheckCommand;
import com.djrapitops.plan.command.commands.webuser.WebDeleteCommand;
import com.djrapitops.plan.command.commands.webuser.WebLevelCommand;
import com.djrapitops.plan.command.commands.webuser.WebListUsersCommand;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;

/**
 * Web subcommand used to manage Web users.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserCommand extends TreeCommand<PlanPlugin> {

    public WebUserCommand(Plan plugin, RegisterCommand register) {
        super(plugin, "webuser, web",
                CommandType.CONSOLE,
                Permissions.MANAGE_WEB.getPerm(),
                Locale.get(Msg.CMD_USG_WEB).toString(),
                "plan web");
        super.setColorScheme(plugin.getColorScheme());
        add(register);
    }

    public WebUserCommand(PlanBungee plugin, RegisterCommand register) {
        super(plugin, "webuser, web",
                CommandType.CONSOLE,
                Permissions.MANAGE_WEB.getPerm(),
                Locale.get(Msg.CMD_USG_WEB).toString(),
                "planbungee web");
        add(register);
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_WEB).toArray();
    }

    @Override
    public void addCommands() {
        add(
                new WebLevelCommand(plugin),
                new WebListUsersCommand(plugin),
                new WebCheckCommand(plugin),
                new WebDeleteCommand(plugin)
        );
    }
}
