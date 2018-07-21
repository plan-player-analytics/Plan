package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;

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
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

        switch (args[0].toLowerCase()) {
            case "kickcount":
                PlanSystem.getInstance().getListenerSystem().setCountKicks(false);
                sender.sendMessage("§aDisabled Kick Counting temporarily until next plugin reload.");
                break;
            default:
                sender.sendMessage("§eDefine a feature to disable! (currently supports only kickCount)");
        }
    }
}
