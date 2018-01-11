package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.Msg;
import com.djrapitops.plan.systems.listeners.PlanPlayerListener;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;

/**
 * This manage SubCommand is used to disable some features of the plugin temporarily.
 *
 * @author Rsl1122
 * @since 4.0.4
 */
public class ManageDisableCommand extends SubCommand {
    /**
     * Class Constructor.
     */
    public ManageDisableCommand() {
        super("disable",
                CommandType.PLAYER_OR_ARGS,
                Permissions.MANAGE.getPermission(),
                "Used to disable some features of the Plugin temporarily",
                "<feature>");
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "kickcount":
                PlanPlayerListener.setCountKicks(false);
                sender.sendMessage("§aDisabled Kick Counting temporarily until next plugin reload.");
                break;
            default:
                sender.sendMessage("§eDefine a feature to disable! (currently supports only kickCount)");
        }
        return true;
    }
}
