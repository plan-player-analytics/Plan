package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.command.commands.manage.*;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;

/**
 * This SubCommand is used to manage the the plugin's database and components.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageCommand extends TreeCmdNode {

    public ManageCommand(PlanPlugin plugin, CommandNode parent) {
        super("manage|m", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, parent);

        Locale locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.MANAGE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE));
        super.setColorScheme(plugin.getColorScheme());
        setNodeGroups(
                new CommandNode[]{
                        new ManageRawDataCommand(plugin),
                        new ManageMoveCommand(plugin),
                        new ManageBackupCommand(plugin),
                        new ManageRestoreCommand(plugin),
                        new ManageRemoveCommand(plugin),
                        new ManageHotSwapCommand(plugin),
                        new ManageClearCommand(plugin),
                },
                new CommandNode[]{
                        new ManageSetupCommand(plugin),
                        new ManageConDebugCommand(plugin),
                        new ManageImportCommand(plugin),
                        new ManageDisableCommand(plugin)
                }
        );
    }
}
