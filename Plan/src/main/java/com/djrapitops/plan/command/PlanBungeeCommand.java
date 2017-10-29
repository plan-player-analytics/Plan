package main.java.com.djrapitops.plan.command;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import com.djrapitops.plugin.command.defaultcmds.StatusCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.PlanBungee;
import main.java.com.djrapitops.plan.command.commands.*;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

/**
 * TreeCommand for the /plan command, and all subcommands.
 * <p>
 * Uses the Abstract Plugin Framework for easier command management.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanBungeeCommand extends TreeCommand<PlanBungee> {

    /**
     * CommandExecutor class Constructor.
     * <p>
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanBungeeCommand(PlanBungee plugin) {
        super(plugin, "planbungee", CommandType.CONSOLE, "", "", "planbungee");
        super.setDefaultCommand("help");
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_PLAN).toArray();
    }

    @Override
    public void addCommands() {
        commands.add(new ReloadCommand(plugin));
        commands.add(new StatusCommand<>(plugin, Permissions.MANAGE.getPermission()));
        commands.add(new ListCommand());
        RegisterCommand registerCommand = new RegisterCommand(plugin);
        commands.add(registerCommand);
        commands.add(new WebUserCommand(plugin, registerCommand));
        commands.add(new NetworkCommand(plugin));
        commands.add(new ListServersCommand(plugin));
    }
}
