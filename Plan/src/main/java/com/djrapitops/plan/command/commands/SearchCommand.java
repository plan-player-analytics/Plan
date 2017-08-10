package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.FormattingUtils;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

import java.util.Arrays;
import java.util.List;

/**
 * This subcommand is used to search for a user, and to view all matches' data.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class SearchCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SearchCommand(Plan plugin) {
        super("search",
                CommandType.CONSOLE_WITH_ARGUMENTS,
                Permissions.SEARCH.getPermission(),
                Locale.get(Msg.CMD_USG_SEARCH).toString(),
                "<part of playername>");
        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        return Locale.get(Msg.CMD_HELP_SEARCH).toArray();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Locale.get(Msg.CMD_FAIL_REQ_ONE_ARG).toString(), sender)) {
            return true;
        }
        sender.sendMessage(Locale.get(Msg.CMD_INFO_SEARCHING).toString());

        runSearchTask(args, sender);
        return true;
    }

    private void runSearchTask(String[] args, ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("SearchTask: " + Arrays.toString(args)) {
            @Override
            public void run() {
                try {
                    List<String> names = MiscUtils.getMatchingPlayerNames(args[0]);
                    sender.sendMessage(Locale.get(Msg.CMD_HEADER_SEARCH) + args[0] + " (" + names.size() + ")");
                    // Results
                    if (names.isEmpty()) {
                        sender.sendMessage(Locale.get(Msg.CMD_INFO_NO_RESULTS).parse(Arrays.toString(args)));
                    } else {
                        sender.sendMessage(Locale.get(Msg.CMD_INFO_RESULTS).toString() + FormattingUtils.collectionToStringNoBrackets(names));
                    }

                    sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
