package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This SubCommand is used to search for a user.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
@Singleton
public class SearchCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public SearchCommand(
            Locale locale,
            Processing processing,
            Database database,
            ErrorHandler errorHandler) {
        super("search", Permissions.SEARCH.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.database = database;
        this.errorHandler = errorHandler;

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
        processing.submitNonCritical(() -> {
            try {
                String searchTerm = args[0];
                List<String> names = database.search().matchingPlayers(searchTerm);
                Collections.sort(names);
                boolean empty = Verify.isEmpty(names);

                sender.sendMessage(locale.getString(CommandLang.HEADER_SEARCH, empty ? 0 : names.size(), searchTerm));
                // Results
                if (!empty) {
                    String message = names.toString();
                    sender.sendMessage(message.substring(1, message.length() - 1));
                }

                sender.sendMessage(">");
            } catch (DBOpException e) {
                sender.sendMessage("§cDatabase error occurred: " + e.getMessage());
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        });
    }
}
