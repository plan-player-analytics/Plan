package main.java.com.djrapitops.plan.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.*;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * CommandExecutor for the /plan command, and all subcommands.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public class PlanCommand implements CommandExecutor {

    private final List<SubCommand> commands;

    /**
     * CommandExecutor class Constructor.
     *
     * Initializes Subcommands
     *
     * @param plugin Current instance of Plan
     */
    public PlanCommand(Plan plugin) {
        commands = new ArrayList<>();
        commands.add(new HelpCommand(plugin, this));
        commands.add(new InspectCommand(plugin));
        commands.add(new QuickInspectCommand(plugin));
        commands.add(new AnalyzeCommand(plugin));
        commands.add(new QuickAnalyzeCommand(plugin));
        commands.add(new SearchCommand(plugin));
        commands.add(new InfoCommand(plugin));
        commands.add(new ReloadCommand(plugin));
        commands.add(new ManageCommand(plugin));
    }

    /**
     * Used to get the list of all subcommands.
     *
     * @return Initialized SubCommands
     */
    public List<SubCommand> getCommands() {
        return this.commands;
    }

    /**
     * Checks SubCommands for matching aliases.
     *
     * @param name SubCommand in text form that might match alias.
     * @return SubCommand, null if no match.
     */
    public SubCommand getCommand(String name) {
        for (SubCommand command : commands) {
            String[] aliases = command.getName().split(",");

            for (String alias : aliases) {
                if (alias.equalsIgnoreCase(name)) {
                    return command;
                }
            }
        }
        return null;
    }

    private void sendDefaultCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String command = "inspect";
        if (args.length < 1) {
            command = "help";
        }
        onCommand(sender, cmd, commandLabel, FormatUtils.mergeArrays(new String[]{command}, args));
    }

    /**
     * Checks if Sender has rights to run the command and executes matching
     * subcommand.
     *
     * @param sender source of the command.
     * @param cmd command.
     * @param commandLabel label.
     * @param args arguments of the command
     * @return true
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Log.debug("Registered command with arguments: "+Arrays.toString(args));
        if (args.length < 1) {
            sendDefaultCommand(sender, cmd, commandLabel, args);
            return true;
        }

        SubCommand command = getCommand(args[0]);

        if (command == null) {
            sendDefaultCommand(sender, cmd, commandLabel, args);
            return true;
        }

        boolean console = !(sender instanceof Player);

        if (!command.getPermission().userHasThisPermission(sender)) {
            sender.sendMessage("" + Phrase.COMMAND_NO_PERMISSION);
            return true;
        }

        if (console && args.length < 2 && command.getCommandType() == CommandType.CONSOLE_WITH_ARGUMENTS) {
            sender.sendMessage("" + Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_PLAN + ""));
            return true;
        }

        if (console && command.getCommandType() == CommandType.PLAYER) {
            sender.sendMessage("" + Phrase.COMMAND_SENDER_NOT_PLAYER);

            return true;
        }

        String[] realArgs = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            realArgs[i - 1] = args[i];
        }

        command.onCommand(sender, cmd, commandLabel, realArgs);
        return true;
    }

}
