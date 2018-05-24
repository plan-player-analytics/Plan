package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.listeners.bukkit.PlayerOnlineListener;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

/**
 * This manage SubCommand is used to disable some features of the plugin temporarily.
 *
 * @author Rsl1122
 * @since 4.0.4
 */
public class ManageDisableCommand extends CommandNode {
    /**
     * Class Constructor.
     */
    public ManageDisableCommand() {
        super("disable", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);
        setShortHelp("Disable a feature temporarily");
        setArguments("<feature>");
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return;
        }
        switch (args[0].toLowerCase()) {
            case "kickcount":
                PlayerOnlineListener.setCountKicks(false);
                sender.sendMessage("§aDisabled Kick Counting temporarily until next plugin reload.");
                break;
            default:
                sender.sendMessage("§eDefine a feature to disable! (currently supports only kickCount)");
        }
    }
}
