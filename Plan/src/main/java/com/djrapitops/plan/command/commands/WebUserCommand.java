package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.command.commands.webuser.WebCheckCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebDeleteCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebLevelCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebListUsersCommand;
import main.java.com.djrapitops.plan.settings.Permissions;
import main.java.com.djrapitops.plan.settings.locale.Locale;
import main.java.com.djrapitops.plan.settings.locale.Msg;

/**
 * Web subcommand used to manage Web users.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserCommand extends TreeCommand<IPlan> {

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
