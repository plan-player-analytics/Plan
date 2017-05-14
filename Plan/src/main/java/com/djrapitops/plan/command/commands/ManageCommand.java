package main.java.com.djrapitops.plan.command.commands;

import java.util.ArrayList;
import java.util.List;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.command.commands.manage.*;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * This command is used to manage the database of the plugin.
 *
 * No arguments will run ManageHelpCommand. Contains subcommands.
 *
 * @author Rsl1122
 * @since 2.3.0
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
        super("manage, m", Permissions.MANAGE, Phrase.CMD_USG_MANAGE + "", CommandType.CONSOLE, "");
        this.plugin = plugin;
        commands = new ArrayList<>();
        commands.add(new ManageHelpCommand(plugin, this));
        commands.add(new ManageMoveCommand(plugin));
        commands.add(new ManageHotswapCommand(plugin));
        commands.add(new ManageBackupCommand(plugin));
        commands.add(new ManageRestoreCommand(plugin));
        commands.add(new ManageStatusCommand(plugin));
        commands.add(new ManageImportCommand(plugin));
        commands.add(new ManageRemoveCommand(plugin));
        commands.add(new ManageClearCommand(plugin));
    }

    /**
     * Used to get the list of manage subcommands.
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
        String command = "help";
        onCommand(sender, cmd, commandLabel, FormatUtils.mergeArrays(new String[]{command}, args));
    }

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

        if (!command.getPermission().userHasThisPermission(sender)) {
            sender.sendMessage("" + Phrase.COMMAND_NO_PERMISSION);
            return true;
        }

        if (console && args.length < 2 && command.getCommandType() == CommandType.CONSOLE_WITH_ARGUMENTS) {
            sender.sendMessage("" + Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_MANAGE + ""));

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
