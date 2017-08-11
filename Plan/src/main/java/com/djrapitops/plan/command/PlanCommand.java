package main.java.com.djrapitops.plan.command;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import com.djrapitops.plugin.command.defaultcmds.StatusCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
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
public class PlanCommand extends TreeCommand<Plan> {

    /**
     * CommandExecutor class Constructor.
     * <p>
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommand(Plan plugin) {
        super(plugin, "plan", CommandType.CONSOLE, "", "", "plan");
        super.setDefaultCommand("inspect");
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_PLAN).toArray();
    }

    @Override
    public void addCommands() {
        commands.add(new InspectCommand(plugin));
        commands.add(new QuickInspectCommand(plugin));
        commands.add(new AnalyzeCommand(plugin));
        commands.add(new QuickAnalyzeCommand(plugin));
        commands.add(new SearchCommand(plugin));
        commands.add(new InfoCommand(plugin));
        commands.add(new ReloadCommand(plugin));
        commands.add(new ManageCommand(plugin));
        commands.add(new StatusCommand<>(plugin, Permissions.MANAGE.getPermission()));
        if (plugin.getUiServer().isEnabled()) {
            commands.add(new ListCommand(plugin));
            RegisterCommand registerCommand = new RegisterCommand(plugin);
            commands.add(registerCommand);
            commands.add(new WebUserCommand(plugin, registerCommand));
        }
    }
}
