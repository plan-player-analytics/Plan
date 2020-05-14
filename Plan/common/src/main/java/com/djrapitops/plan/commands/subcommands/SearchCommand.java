/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.commands.subcommands;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
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
 */
@Singleton
public class SearchCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public SearchCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorLogger errorLogger) {
        super("search", Permissions.SEARCH.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;

        setArguments("<text>");
        setShortHelp(locale.getString(CmdHelpLang.SEARCH));
        setInDepthHelp(locale.getArray(DeepHelpLang.SEARCH));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

        runSearchTask(args, sender);
    }

    private void runSearchTask(String[] args, Sender sender) {
        processing.submitNonCritical(() -> {
            try {
                String searchFor = args[0];
                List<String> names = dbSystem.getDatabase().query(UserIdentifierQueries.fetchMatchingPlayerNames(searchFor));
                Collections.sort(names);
                boolean empty = Verify.isEmpty(names);

                sender.sendMessage(locale.getString(CommandLang.HEADER_SEARCH, empty ? 0 : names.size(), searchFor));
                // Results
                if (!empty) {
                    String message = names.toString();
                    sender.sendMessage(message.substring(1, message.length() - 1));
                }

                sender.sendMessage(">");
            } catch (DBOpException e) {
                sender.sendMessage("§cDatabase error occurred: " + e.getMessage());
                errorLogger.log(L.ERROR, this.getClass(), e);
            }
        });
    }
}
