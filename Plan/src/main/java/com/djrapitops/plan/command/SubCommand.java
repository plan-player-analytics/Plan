package main.java.com.djrapitops.plan.command;

import main.java.com.djrapitops.plan.Permissions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Abstract subcommand class that stores all the required information of a
 * command.
 *
 * @author Rsl1122
 * @since 1.0.0
 */
public abstract class SubCommand {

    private final String name;
    private final Permissions permission;
    private final String usage;
    private final CommandType commandType;
    private final String arguments;

    /**
     * Class constructor, called with super(...) in subcommands.
     *
     * @param name Name(s) (aliases) of the command
     * @param permission Required permission
     * @param usage Usage information
     * @param commandType Type Enum
     * @param arguments Additional possible arguments the command requires
     */
    public SubCommand(String name, Permissions permission, String usage, CommandType commandType, String arguments) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.commandType = commandType;
        this.arguments = arguments;
    }

    /**
     * Used to get a string format of required arguments.
     *
     * @return Additional possible arguments the command requires
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Used to get the first alias.
     *
     * @return First alias of the command
     */
    public String getFirstName() {
        return name.split(",")[0];
    }

    /**
     * Used to get all aliases.
     *
     * @return All aliases separated with ','
     */
    public String getName() {
        return name;
    }

    /**
     * Used to get the permission required by the command.
     *
     * @return Required permission
     */
    public Permissions getPermission() {
        return permission;
    }

    /**
     * Used to get the info about usage of the command.
     *
     * @return Usage information
     */
    public String getUsage() {
        return usage;
    }

    /**
     * Used to get the command type.
     *
     * @return CommandType Enum.
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * The Command Execution method.
     *
     * @param sender Parameter of onCommand in CommandExecutor.
     * @param cmd Parameter of onCommand in CommandExecutor.
     * @param commandLabel Parameter of onCommand in CommandExecutor.
     * @param args Parameter of onCommand in CommandExecutor.
     * @return Was the execution successful?
     * @see CommandExecutor
     */
    public abstract boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
