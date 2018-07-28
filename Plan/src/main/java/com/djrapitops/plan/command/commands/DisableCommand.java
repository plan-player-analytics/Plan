package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

public class DisableCommand extends CommandNode {

    public DisableCommand() {
        super("disable", "plan.reload", CommandType.ALL);
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        PlanPlugin.getInstance().onDisable();
        sender.sendMessage(
                "§aPlan systems are now disabled. " +
                        "You can still use /planbungee reload to restart the plugin."
        );
    }
}
