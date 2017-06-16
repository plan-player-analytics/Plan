package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.TreeCommand;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.manage.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        super(plugin, new SubCommand("manage,m", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE + "") {
            @Override
            public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
                return true;
            }
        }, "plan manage");
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
