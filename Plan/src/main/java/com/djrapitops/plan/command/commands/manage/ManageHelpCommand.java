package main.java.com.djrapitops.plan.command.commands.manage;

import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.command.commands.ManageCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class ManageHelpCommand extends SubCommand {

    private final Plan plugin;
    private final ManageCommand command;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     * @param command Current instance of PlanCommand
     */
    public ManageHelpCommand(Plan plugin, ManageCommand command) {
        super("help,?", "plan.manage", Phrase.CMD_USG_MANAGE_HELP + "", CommandType.CONSOLE, "");

        this.plugin = plugin;
        this.command = command;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();

        // Header
        sender.sendMessage(Phrase.CMD_MANAGE_HELP_HEADER + "");
        // Help results
        for (SubCommand command : this.command.getCommands()) {
            if (command.getName().equalsIgnoreCase(getName())) {
                continue;
            }

            if (!sender.hasPermission(command.getPermission())) {
                continue;
            }

            if (!(sender instanceof Player) && command.getCommandType() == CommandType.PLAYER) {
                continue;
            }

            sender.sendMessage(tColor + " " + Phrase.BALL.toString() + oColor
                    + " /plan manage " + command.getFirstName() + " " + command.getArguments() + tColor + " - " + command.getUsage());
        }
        // Footer
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }

}
