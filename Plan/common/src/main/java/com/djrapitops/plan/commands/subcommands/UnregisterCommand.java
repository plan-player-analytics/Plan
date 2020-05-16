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

import com.djrapitops.plan.commands.use.Arguments;
import com.djrapitops.plan.delivery.domain.auth.User;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemoveWebUserTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.CommandUtils;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Subcommand for deleting a WebUser.
 *
 * @author Rsl1122
 */
@Singleton
public class UnregisterCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final UUIDUtility uuidUtility;
    private final ErrorLogger errorLogger;

    @Inject
    public UnregisterCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            UUIDUtility uuidUtility,
            ErrorLogger errorLogger
    ) {
        super("unregister", "", CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.uuidUtility = uuidUtility;
        this.errorLogger = errorLogger;

        setShortHelp(locale.getString(CmdHelpLang.WEB_DELETE));
        setArguments("[username]");
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_DATABASE_NOT_OPEN, dbState.name()));
            return;
        }

        Arguments arguments = new Arguments(args);

        Optional<String> givenUsername = arguments.get(0);

        processing.submitNonCritical(() -> {
            Database database = dbSystem.getDatabase();
            try {
                UUID playerUUID = CommandUtils.isPlayer(sender) ? uuidUtility.getUUIDOf(sender.getName()) : null;

                String username;
                if (!givenUsername.isPresent() && playerUUID != null) {
                    Optional<User> found = database.query(WebUserQueries.fetchUser(playerUUID));
                    if (!found.isPresent()) {
                        sender.sendMessage("§c" + locale.getString(FailReason.USER_DOES_NOT_EXIST));
                        return;
                    }
                    username = found.get().getUsername();
                } else if (!givenUsername.isPresent()) {
                    sender.sendMessage("§c" + locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments())));
                    return;
                } else {
                    username = givenUsername.get();
                }

                Optional<User> found = database.query(WebUserQueries.fetchUser(username));
                if (!found.isPresent()) {
                    sender.sendMessage("§c" + locale.getString(FailReason.USER_DOES_NOT_EXIST));
                    return;
                }
                User presentUser = found.get();
                boolean linkedToSender = Objects.equals(playerUUID, presentUser.getLinkedToUUID());
                if (linkedToSender || sender.hasPermission(Permissions.MANAGE_WEB.getPerm())) {
                    deleteUser(sender, database, username);
                } else {
                    sender.sendMessage("§c" + locale.getString(CommandLang.USER_NOT_LINKED));
                }
            } catch (Exception e) {
                errorLogger.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

    private void deleteUser(Sender sender, Database database, String username) throws InterruptedException, java.util.concurrent.ExecutionException {
        sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));
        database.executeTransaction(new RemoveWebUserTransaction(username))
                .get(); // Wait for completion
        sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
    }

}
