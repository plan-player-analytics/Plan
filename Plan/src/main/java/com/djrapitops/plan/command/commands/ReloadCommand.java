package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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

    public ReloadCommand(PlanPlugin plugin) {
        super("reload", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);
        setShortHelp(Locale.get(Msg.CMD_USG_RELOAD).toString());
        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        try {
            plugin.reloadPlugin(true);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
            sender.sendMessage("§cSomething went wrong during reload of the plugin, a restart is recommended.");
        }
        sender.sendMessage(Locale.get(Msg.CMD_INFO_RELOAD_COMPLETE).toString());
    }
}
