package com.djrapitops.plan.common.command.commands;

import com.djrapitops.plan.common.PlanPlugin;
import com.djrapitops.plan.common.command.commands.manage.*;
import com.djrapitops.plan.common.system.settings.Permissions;
import com.djrapitops.plan.common.system.settings.locale.Locale;
import com.djrapitops.plan.common.system.settings.locale.Msg;
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
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE).toString());
        setInDepthHelp(Locale.get(Msg.CMD_HELP_MANAGE).toArray());
        super.setColorScheme(plugin.getColorScheme());
        setNodeGroups(
                new CommandNode[]{
                        new ManageMoveCommand(),
                        new ManageBackupCommand(),
                        new ManageRestoreCommand(plugin),
                        new ManageRemoveCommand(),
                        new ManageHotSwapCommand(plugin),
                        new ManageClearCommand(),
                },
                new CommandNode[]{
                        new ManageSetupCommand(),
                        new ManageConDebugCommand(),
                        new ManageImportCommand(),
                        new ManageDisableCommand()
                }
        );
    }
}
