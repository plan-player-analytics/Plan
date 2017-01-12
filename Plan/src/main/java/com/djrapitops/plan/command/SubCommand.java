package com.djrapitops.plan.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public abstract class SubCommand {

    private final String name;
    private final String permission;
    private final String usage;
    private final CommandType commandType;
    private final String arguments;

    public SubCommand(String name, String permission, String usage, CommandType commandType, String arguments) {
        this.name = name;
        this.permission = permission;
        this.usage = usage;
        this.commandType = commandType;
        this.arguments = arguments;
    }

    public String getArguments() {
        return arguments;
    }
    
    public String getFirstName() {
        return name.split(",")[0];
    }

    public String getName() {
        return name;
    }

    public String getPermission() {
        return permission;
    }

    public String getUsage() {
        return usage;
    }

    public CommandType getCommandType() {
        return commandType;
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args);
}
