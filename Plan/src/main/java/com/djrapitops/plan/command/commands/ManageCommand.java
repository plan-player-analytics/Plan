package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.commands.manage.*;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;

/**
 * This command is used to manage the database of the plugin.
 * <p>
 * No arguments will run ManageHelpCommand. Contains subcommands.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageCommand extends TreeCommand<Plan> {

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageCommand(Plan plugin) {
        super(plugin, "manage,m", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Locale.get(Msg.CMD_USG_MANAGE).toString(), "plan m");
        super.setColorScheme(plugin.getColorScheme());
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE).toArray();
    }

    @Override
    public void addCommands() {
        add(
                new ManageMoveCommand(),
                new ManageHotswapCommand(plugin),
                new ManageBackupCommand(),
                new ManageRestoreCommand(plugin),
                new ManageImportCommand(),
                new ManageRemoveCommand(),
                new ManageClearCommand(plugin),
                new ManageSetupCommand(),
                new ManageDisableCommand()
        );
    }
}
