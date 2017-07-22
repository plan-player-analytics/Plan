package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.webuser.WebCheckCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebDeleteCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebLevelCommand;
import main.java.com.djrapitops.plan.command.commands.webuser.WebListUsersCommand;

/**
 * Web subcommand used to manage Web users.
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class WebUserCommand extends TreeCommand<Plan> {

    public WebUserCommand(Plan plugin, RegisterCommand register) {
        super(plugin, "webuser, web", CommandType.CONSOLE, Permissions.MANAGE_WEB.getPerm(), "Manage Webusers", "plan web");
        commands.add(register);
    }

    @Override
    public void addCommands() {
        commands.add(new WebLevelCommand(plugin));
        commands.add(new WebListUsersCommand(plugin));
        commands.add(new WebCheckCommand(plugin));
        commands.add(new WebDeleteCommand(plugin));
    }
}
