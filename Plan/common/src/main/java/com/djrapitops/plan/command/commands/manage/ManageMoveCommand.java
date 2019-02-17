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
package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.db.DBType;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.BackupCopyTransaction;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.CommandLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * This manage SubCommand is used to move all data from one database to another.
 * <p>
 * Destination database will be cleared.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageMoveCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageMoveCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("move", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setArguments("<fromDB>", "<toDB>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_MOVE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_MOVE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 2, Arrays.toString(this.getArguments()))));

        String fromDB = args[0].toLowerCase();
        boolean isCorrectDB = DBType.exists(fromDB);
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, fromDB)));

        String toDB = args[1].toLowerCase();
        isCorrectDB = DBType.exists(toDB);
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, toDB)));

        Verify.isFalse(fromDB.equalsIgnoreCase(toDB),
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));

        if (!Verify.contains("-a", args)) {
            sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_OVERWRITE, toDB)));
            return;
        }

        try {
            final Database fromDatabase = dbSystem.getActiveDatabaseByName(fromDB);
            final Database toDatabase = dbSystem.getActiveDatabaseByName(toDB);
            fromDatabase.init();
            toDatabase.init();

            runMoveTask(fromDatabase, toDatabase, sender);
        } catch (Exception e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runMoveTask(final Database fromDatabase, final Database toDatabase, Sender sender) {
        processing.submitCritical(() -> {
            try {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                toDatabase.executeTransaction(new BackupCopyTransaction(fromDatabase, toDatabase));

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));

                boolean movingToCurrentDB = toDatabase.getType() == dbSystem.getDatabase().getType();
                if (movingToCurrentDB) {
                    sender.sendMessage(locale.getString(ManageLang.HOTSWAP_REMINDER, toDatabase.getType().getConfigName()));
                }
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}
