package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.DefaultMessages;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.FormattingUtils;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
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
        super("search", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.SEARCH.getPermission(), Phrase.CMD_USG_SEARCH + "", Phrase.ARG_SEARCH + "");
        this.plugin = plugin;
        setHelp(plugin);
    }

    private void setHelp(Plan plugin) {
        ColorScheme colorScheme = plugin.getColorScheme();

        String ball = DefaultMessages.BALL.toString();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        String[] help = new String[]{
                mCol +"Search command",
                tCol+"  Used to get a list of Player names that match the given argument.",
                sCol+"  Example: /plan search 123 - Finds all users with 123 in their name."
        };
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString(), sender)) {
            return true;
        }
        sender.sendMessage(Phrase.CMD_SEARCH_SEARCHING + "");

        runSearchTask(args, sender);
        return true;
    }

    private void runSearchTask(String[] args, ISender sender) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("SearchTask: " + Arrays.toString(args)) {
            @Override
            public void run() {
                try {
                    List<String> names = MiscUtils.getMatchingPlayerNames(args[0]);
                    sender.sendMessage(Phrase.CMD_SEARCH_HEADER + args[0] + " (" + names.size() + ")");
                    // Results
                    if (names.isEmpty()) {
                        sender.sendMessage(Phrase.CMD_NO_RESULTS.parse(Arrays.toString(args)));
                    } else {
                        sender.sendMessage(Phrase.CMD_MATCH + "" + FormattingUtils.collectionToStringNoBrackets(names));
                    }
                    sender.sendMessage(Phrase.CMD_FOOTER.toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
