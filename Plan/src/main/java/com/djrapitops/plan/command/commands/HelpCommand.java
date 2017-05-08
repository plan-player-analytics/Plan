package main.java.com.djrapitops.plan.command.commands;

import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.PlanCommand;
import main.java.com.djrapitops.plan.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This subcommand is used to view the subcommands.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class HelpCommand extends SubCommand {

    private final Plan plugin;
    private final PlanCommand command;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     * @param command Current instance of PlanCommand
     */
    public HelpCommand(Plan plugin, PlanCommand command) {
        super("help,?", Permissions.HELP, Phrase.CMD_USG_HELP + "", CommandType.CONSOLE, "");

        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command comd, String commandLabel, String[] args) {
        boolean isConsole = !(sender instanceof Player);
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();

        sender.sendMessage(Phrase.CMD_HELP_HEADER + "");
        
        this.command.getCommands().stream()
                .filter(cmd -> !cmd.getName().equalsIgnoreCase(getName()))
                .filter(cmd -> cmd.getPermission().userHasThisPermission(sender))
                .filter(cmd -> !(isConsole && cmd.getCommandType() == CommandType.PLAYER))
                .map(cmd -> tColor + " " + Phrase.BALL.toString() + oColor + " /plan " + cmd.getFirstName() + " " + cmd.getArguments() + tColor + " - " + cmd.getUsage())
                .forEach(msg -> sender.sendMessage(msg));
        sender.sendMessage(Phrase.CMD_FOOTER + "");
        return true;
    }
}
