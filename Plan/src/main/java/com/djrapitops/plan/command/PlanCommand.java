package main.java.com.djrapitops.plan.command;

import com.djrapitops.javaplugin.command.*;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.*;

/**
 * CommandExecutor for the /plan command, and all subcommands.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanCommand extends TreeCommand<Plan> {

    /**
     * CommandExecutor class Constructor.
     *
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommand(Plan plugin) {
        super(plugin, "plan", CommandType.CONSOLE, "", "", "plan");
        super.setDefaultCommand("inspect");
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
        commands.add(new StatusCommand(plugin, Permissions.MANAGE.getPermission()));
    }
}
