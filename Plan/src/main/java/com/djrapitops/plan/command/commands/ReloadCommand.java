package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;

/**
 * This SubCommand is used to reload the plugin.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class ReloadCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;

    public ReloadCommand(PlanPlugin plugin) {
        super("reload", Permissions.RELOAD.getPermission(), CommandType.CONSOLE);
        this.plugin = plugin;

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.RELOAD));
        setInDepthHelp(locale.getArray(DeepHelpLang.RELOAD));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        try {
            plugin.reloadPlugin(true);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            sender.sendMessage(locale.getString(CommandLang.RELOAD_FAILED));
        }
        sender.sendMessage(locale.getString(CommandLang.RELOAD_COMPLETE));
    }
}
