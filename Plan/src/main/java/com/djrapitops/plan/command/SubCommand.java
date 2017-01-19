package com.djrapitops.plan.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rsl1122
 */
public abstract class SubCommand {

    private final String name;
    private final String permission;
    private final String usage;
    private final CommandType commandType;
    private final String arguments;

    /**
     * Class constructor, called with super(...) in subcommands.
     * @param name Name(s) (aliases) of the command
     * @param permission Required permission
     * @param usage Usage information
     * @param commandType Type Enum
     * @param arguments Additional possible arguments the command requires
     */
    public SubCommand(String name, String permission, String usage, CommandType commandType, String arguments) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.commandType = commandType;
        this.arguments = arguments;
    }

    /**
     * @return Additional possible arguments the command requires
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * @return First alias of the command
     */
    public String getFirstName() {
        return name.split(",")[0];
    }

    /**
     * @return All aliases
     */
    public String getName() {
        return name;
    }

    /**
     * @return Required permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * @return Usage information
     */
    public String getUsage() {
        return usage;
    }

    /**
     * @return CommandType Enum.
     */
    public CommandType getCommandType() {
        return commandType;
    }

    /**
     * The Command Execution method.
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args
     * @return
     */
    public abstract boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
