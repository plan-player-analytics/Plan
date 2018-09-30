package com.djrapitops.plugin.command.velocity;

import com.djrapitops.plugin.command.CommandNode;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;

/**
 * Class that is used to wrap a CommandNode implementation into executable
 * command by Velocity.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class VelocityCommand implements Command {

    private final CommandNode commandNode;

    public VelocityCommand(CommandNode commandNode) {
        this.commandNode = commandNode;
    }

    @Override
    public void execute(CommandSource sender, String[] args) {
        VelocityCMDSender iSender = new VelocityCMDSender(sender);
        commandNode.onCommand(iSender, "", args);
    }
}
