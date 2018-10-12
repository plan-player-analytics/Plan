package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.command.commands.manage.*;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.ColorScheme;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCmdNode;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This SubCommand is used to manage the the plugin's database and components.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageCommand extends TreeCmdNode {

    @Inject
    public ManageCommand(ColorScheme colorScheme, Locale locale, @Named("mainCommand") Lazy<CommandNode> parent,
                         // Group 1
                         ManageRawDataCommand rawDataCommand,
                         ManageMoveCommand moveCommand,
                         ManageBackupCommand backupCommand,
                         ManageRemoveCommand removeCommand,
                         ManageRestoreCommand restoreCommand,
                         ManageHotSwapCommand hotSwapCommand,
                         ManageClearCommand clearCommand,
                         // Group 2
                         ManageSetupCommand setupCommand,
                         ManageConDebugCommand conDebugCommand,
                         ManageImportCommand importCommand,
                         ManageDisableCommand disableCommand,
                         ManageUninstalledCommand uninstalledCommand
    ) {
        super("manage|m", Permissions.MANAGE.getPermission(), CommandType.CONSOLE, parent.get());
        super.setColorScheme(colorScheme);

        setShortHelp(locale.getString(CmdHelpLang.MANAGE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE));
        CommandNode[] databaseGroup = {
                rawDataCommand,
                moveCommand,
                backupCommand,
                restoreCommand,
                hotSwapCommand,
                removeCommand,
                clearCommand,
        };
        CommandNode[] pluginGroup = {
                setupCommand,
                conDebugCommand,
                importCommand,
                disableCommand,
                uninstalledCommand
        };
        setNodeGroups(databaseGroup, pluginGroup);
    }
}
