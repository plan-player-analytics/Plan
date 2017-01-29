package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.utilities.FormatUtils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.ArrayList;
import main.java.com.djrapitops.plan.command.commands.manage.*;
import org.bukkit.entity.Player;

/**
 *
 * @author Rsl1122
 */
public class ManageCommand extends SubCommand {

    private final List<SubCommand> commands;
    private Plan plugin;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageCommand(Plan plugin) {
        super("manage", "plan.manage", "Database managment command", CommandType.CONSOLE, "");
        this.plugin = plugin;
        commands = new ArrayList<>();
        commands.add(new ManageHelpCommand(plugin, this));
        commands.add(new ManageMoveCommand(plugin));
        commands.add(new ManageCombineCommand(plugin));
        commands.add(new ManageHotswapCommand(plugin));
        commands.add(new ManageStatusCommand(plugin));
        commands.add(new ManageRemoveCommand(plugin));
        commands.add(new ManageClearCommand(plugin));        
    }

    /**
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
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
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

        if (!sender.hasPermission(command.getPermission())) {
            sender.sendMessage("" + Phrase.COMMAND_NO_PERMISSION);
            return true;
        }

        if (console && args.length < 2 && command.getCommandType() == CommandType.CONSOLE_WITH_ARGUMENTS) {
            sender.sendMessage("" + Phrase.COMMAND_REQUIRES_ARGUMENTS);

            return true;
        }

        if (console && command.getCommandType() == CommandType.PLAYER) {;
            sender.sendMessage("" + Phrase.COMMAND_SENDER_NOT_PLAYER);

            return true;
        }

        String[] realArgs = new String[args.length - 1];

        for (int i = 1; i < args.length; i++) {
            realArgs[i - 1] = args[i];
        }

        if (!command.onCommand(sender, cmd, commandLabel, realArgs)) {
//            Phrase.TRY_COMMAND.sendWithPrefix( sender, parse( commandLabel, command ) );
        }
        return true;
    }

}
