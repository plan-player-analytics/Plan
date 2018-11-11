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

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.DBType;
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
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;

/**
 * This manage SubCommand is used to clear a database of all data.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
@Singleton
public class ManageClearCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageClearCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super("clear", Permissions.MANAGE.getPermission(), CommandType.PLAYER_OR_ARGS);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.errorHandler = errorHandler;

        setArguments("<DB>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_CLEAR));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_CLEAR));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 1,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

        String dbName = args[0].toLowerCase();

        boolean isCorrectDB = DBType.exists(dbName);
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

        if (!Verify.contains("-a", args)) {
            sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_REMOVAL, dbName)));
            return;
        }

        try {
            Database database = dbSystem.getActiveDatabaseByName(dbName);
            database.init();
            runClearTask(sender, database);
        } catch (DBInitException e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runClearTask(Sender sender, Database database) {
        processing.submitCritical(() -> {
            try {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                database.remove().everything();

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (DBOpException e) {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        });
    }
}
