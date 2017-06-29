package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslTask;
import com.djrapitops.javaplugin.utilities.FormattingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.Condition;
import main.java.com.djrapitops.plan.data.cache.InspectCacheHandler;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This subcommand is used to search for a user, and to view all matches' data.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class SearchCommand extends SubCommand {

    private final Plan plugin;
    private final InspectCacheHandler inspectCache;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public SearchCommand(Plan plugin) {
        super("search", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.SEARCH.getPermission(), Phrase.CMD_USG_SEARCH + "", Phrase.ARG_SEARCH + "");
        this.plugin = plugin;
        inspectCache = plugin.getInspectCache();
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        Condition c = new Condition(args.length != 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString());
        if (c.pass()) {
            sender.sendMessage(c.getFailMsg());
            return true;
        }
        sender.sendMessage(Phrase.CMD_SEARCH_SEARCHING + "");
        final RslTask searchTask = new RslBukkitRunnable<Plan>("SearchTask: " + Arrays.toString(args)) {
            @Override
            public void run() {
                try {
                    Set<OfflinePlayer> matches = MiscUtils.getMatchingDisplaynames(args[0]);
                    sender.sendMessage(Phrase.CMD_SEARCH_HEADER + args[0] + " (" + matches.size() + ")");
                    // Results
                    if (matches.isEmpty()) {
                        sender.sendMessage(Phrase.CMD_NO_RESULTS.parse(Arrays.toString(args)));
                    } else {
                        List<String> names = matches.stream().map(p -> p.getName()).collect(Collectors.toList());
                        Collections.sort(names);
                        sender.sendMessage(Phrase.CMD_MATCH + "" + FormattingUtils.collectionToStringNoBrackets(names));
                    }
                    sender.sendMessage(Phrase.CMD_FOOTER + "");
                } finally {
                    this.cancel();
                }
            }
        }.runTaskAsynchronously();
        return true;
    }
}
