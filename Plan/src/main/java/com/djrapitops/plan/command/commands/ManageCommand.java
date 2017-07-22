package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.manage.*;

/**
 * This command is used to manage the database of the plugin.
 *
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
        super(plugin, "manage,m", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE + "", "plan m");
    }

    @Override
    public void addCommands() {
        commands.add(new ManageMoveCommand(plugin));
        commands.add(new ManageHotswapCommand(plugin));
        commands.add(new ManageBackupCommand(plugin));
        commands.add(new ManageRestoreCommand(plugin));
        commands.add(new ManageStatusCommand(plugin));
        commands.add(new ManageImportCommand(plugin));
        commands.add(new ManageRemoveCommand(plugin));
//        commands.add(new ManageCleanCommand(plugin));
        commands.add(new ManageClearCommand(plugin));
    }
}
