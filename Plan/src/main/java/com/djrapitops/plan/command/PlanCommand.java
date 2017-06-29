package main.java.com.djrapitops.plan.command;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.StatusCommand;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.TreeCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.AnalyzeCommand;
import main.java.com.djrapitops.plan.command.commands.InfoCommand;
import main.java.com.djrapitops.plan.command.commands.InspectCommand;
import main.java.com.djrapitops.plan.command.commands.ManageCommand;
import main.java.com.djrapitops.plan.command.commands.QuickAnalyzeCommand;
import main.java.com.djrapitops.plan.command.commands.QuickInspectCommand;
import main.java.com.djrapitops.plan.command.commands.ReloadCommand;
import main.java.com.djrapitops.plan.command.commands.SearchCommand;
import org.bukkit.command.Command;

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
        super(plugin, new SubCommand("plan", CommandType.CONSOLE, "") {
            @Override
            public boolean onCommand(ISender sender, String commandLabel, String[] args) {
                return true;
            }
        }, "plan");
        super.setDefaultCommand("inspect");
//        commands.add(new HelpCommand(plugin, this));

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
