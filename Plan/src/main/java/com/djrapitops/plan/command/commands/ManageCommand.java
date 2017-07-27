package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.TreeCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.DefaultMessages;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.commands.manage.*;

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
        super(plugin, "manage,m", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE + "", "plan m");
        setHelp(plugin);
    }

    private void setHelp(Plan plugin) {
        ColorScheme colorScheme = plugin.getColorScheme();

        String ball = DefaultMessages.BALL.toString();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        String[] help = new String[]{
                mCol +"Manage command",
                tCol+"  Used to Manage Database of the plugin.",
                sCol+"  Alias: /plan m",
                sCol+"  /plan m - List subcommands",
                sCol+"  /plan m <subcommand> ? - in depth help"
        };
    }

    @Override
    public void addCommands() {
//        commands.add(new ManageMoveCommand(plugin));
        commands.add(new ManageHotswapCommand(plugin));
//        commands.add(new ManageBackupCommand(plugin));
//        commands.add(new ManageRestoreCommand(plugin));
        commands.add(new ManageStatusCommand(plugin));
        commands.add(new ManageImportCommand(plugin));
        commands.add(new ManageRemoveCommand(plugin));
//        commands.add(new ManageCleanCommand(plugin));
        commands.add(new ManageClearCommand(plugin));
    }
}
