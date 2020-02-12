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
package com.djrapitops.plan.commands.subcommands.manage;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.UUIDUtility;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.query.QuerySvc;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.PlayerFetchQueries;
import com.djrapitops.plan.storage.database.transactions.commands.RemovePlayerTransaction;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * This manage subcommand is used to remove a single player's data from the
 * dbSystem.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageRemoveCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final QuerySvc queryService;
    private final UUIDUtility uuidUtility;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageRemoveCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            QuerySvc queryService,
            UUIDUtility uuidUtility,
            ErrorHandler errorHandler
    ) {
        super("remove|delete", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.queryService = queryService;
        this.uuidUtility = uuidUtility;
        this.errorHandler = errorHandler;

        setArguments("<player>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_REMOVE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_REMOVE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String playerName = MiscUtils.getPlayerName(args, sender, Permissions.MANAGE);

        if (playerName == null) {
            sender.sendMessage(locale.getString(CommandLang.FAIL_NO_PERMISSION));
            return;
        }

        runRemoveTask(playerName, sender, args);
    }

    private void runRemoveTask(String playerName, Sender sender, String[] args) {
        processing.submitCritical(() -> {
            try {
                UUID playerUUID = uuidUtility.getUUIDOf(playerName);

                if (playerUUID == null) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_VALID));
                    return;
                }

                Database db = dbSystem.getDatabase();
                if (!db.query(PlayerFetchQueries.isPlayerRegistered(playerUUID))) {
                    sender.sendMessage(locale.getString(CommandLang.FAIL_USERNAME_NOT_KNOWN));
                    return;
                }

                if (!Verify.contains("-a", args)) {
                    sender.sendMessage(
                            locale.getString(ManageLang.CONFIRMATION,
                                    locale.getString(ManageLang.CONFIRM_REMOVAL, db.getType().getName())
                            )
                    );
                    return;
                }

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));
                queryService.playerRemoved(playerUUID);
                db.executeTransaction(new RemovePlayerTransaction(playerUUID))
                        .get(); // Wait for completion
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (DBOpException | ExecutionException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}
