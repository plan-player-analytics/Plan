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
 *
 * @author Rsl1122
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
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();

        // Header
        sender.sendMessage(Phrase.CMD_HELP_HEADER + "");
        // Help results
        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!command.getPermission().userHasThisPermission(sender)) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(tColor + " " + Phrase.BALL.toString() + oColor
                    + " /plan " + command.getFirstName() + " " + command.getArguments() + tColor + " - " + command.getUsage());
        }
        // Footer
        sender.sendMessage(Phrase.CMD_FOOTER + "");
        return true;
    }

}
