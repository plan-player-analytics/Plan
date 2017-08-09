package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.task.AbsRunnable;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.utilities.dump.DumpUtils;

/**
 * This manage subcommand is used to dump important data to pastebin,
 * so it's easier to write an issue.
 *
 * @author Fuzzlemann
 * @since 3.7.0
 */
public class ManageDumpCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageDumpCommand(Plan plugin) {
        super("dump", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_CLEAR.toString());

        this.plugin = plugin;
    }

    @Override
    public String[] addHelp() {
        ColorScheme colorScheme = plugin.getColorScheme();

        String mCol = colorScheme.getMainColor();
        String tCol = colorScheme.getTertiaryColor();

        return new String[]{
                mCol + "Manage Dump command",
                tCol + "  Used to dump important data for bug reporting to hastebin.",
        };
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        dump(sender);
        return true;
    }

    private void dump(ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("DumpTask") {
            @Override
            public void run() {
                sender.sendLink("Link to the Dump", DumpUtils.dump(plugin));
                sender.sendLink("Report Issues here", "https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/new");
            }
        }).runTaskAsynchronously();
    }
}
