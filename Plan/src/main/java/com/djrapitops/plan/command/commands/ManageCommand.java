package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.manage.*;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

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

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE).toArray();
    }

    @Override
    public void addCommands() {
//        commands.add(new ManageMoveCommand(plugin));
        commands.add(new ManageHotswapCommand(plugin));
//        commands.add(new ManageBackupCommand(plugin));
//        commands.add(new ManageRestoreCommand(plugin));
        commands.add(new ManageImportCommand(plugin));
        commands.add(new ManageRemoveCommand(plugin));
//        commands.add(new ManageCleanCommand(plugin));
        commands.add(new ManageClearCommand(plugin));
        commands.add(new ManageDumpCommand(plugin));
    }
}
