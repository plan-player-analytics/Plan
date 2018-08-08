package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.FormatUtils;
import com.djrapitops.plugin.utilities.Verify;

import java.util.Arrays;
import java.util.List;

/**
 * This SubCommand is used to search for a user.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class SearchCommand extends CommandNode {

    private final Locale locale;

    public SearchCommand(PlanPlugin plugin) {
        super("search", Permissions.SEARCH.getPermission(), CommandType.PLAYER_OR_ARGS);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("<text>");
        setShortHelp(locale.getString(CmdHelpLang.SEARCH));
        setInDepthHelp(locale.getArray(DeepHelpLang.SEARCH));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

        runSearchTask(args, sender);
    }

    private void runSearchTask(String[] args, ISender sender) {
        RunnableFactory.createNew(new AbsRunnable("SearchTask: " + Arrays.toString(args)) {
            @Override
            public void run() {
                try {
                    String searchTerm = args[0];
                    List<String> names = MiscUtils.getMatchingPlayerNames(searchTerm);

                    boolean empty = Verify.isEmpty(names);

                    sender.sendMessage(locale.getString(CommandLang.HEADER_SEARCH, empty ? 0 : names.size(), searchTerm));
                    // Results
                    if (!empty) {
                        sender.sendMessage(FormatUtils.collectionToStringNoBrackets(names));
                    }

                    sender.sendMessage(">");
                } catch (DBOpException e) {
                    sender.sendMessage("§cDatabase error occurred: " + e.getMessage());
                    Log.toLog(this.getClass(), e);
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
