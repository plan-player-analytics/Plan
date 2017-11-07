package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.file.dump.DumpUtils;

/**
 * This manage subcommand is used to dump important data to pastebin, so it's
 * easier to write an issue.
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
        super("dump",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_DUMP).toString());

        this.plugin = plugin;
    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_MANAGE_DUMP).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        dump(sender);
        return true;
    }

    private void dump(ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("DumpTask") {
            @Override
            public void run() {
                try {
                    sender.sendLink("Link to the Dump", DumpUtils.dump(plugin));
                    sender.sendLink("Report Issues here", "https://github.com/Rsl1122/Plan-PlayerAnalytics/issues/new");
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
