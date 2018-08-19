package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.listeners.bukkit.PlayerOnlineListener;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.Arrays;

/**
 * This manage SubCommand is used to disable some features of the plugin temporarily.
 *
 * @author Rsl1122
 * @since 4.0.4
 */
public class ManageDisableCommand extends CommandNode {

    private final Locale locale;

    @Inject
    public ManageDisableCommand(Locale locale) {
        super("disable", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;

        setArguments("<feature>");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_DISABLE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_DISABLE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        switch (args[0].toLowerCase()) {
            case "kickcount":
                PlayerOnlineListener.setCountKicks(false);
                sender.sendMessage(locale.getString(CommandLang.FEATURE_DISABLED, "Kick Counting"));
                break;
            default:
                sender.sendMessage(locale.getString(CommandLang.FAIL_NO_SUCH_FEATURE, "'kickcount'"));
        }
    }
}
