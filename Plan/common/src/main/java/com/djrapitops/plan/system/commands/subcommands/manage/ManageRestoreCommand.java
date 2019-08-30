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
package com.djrapitops.plan.system.commands.subcommands.manage;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.settings.locale.lang.CommandLang;
import com.djrapitops.plan.system.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.locale.lang.ManageLang;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.DBType;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.SQLiteDB;
import com.djrapitops.plan.system.storage.database.access.transactions.BackupCopyTransaction;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.io.File;
import java.util.Arrays;

/**
 * This manage SubCommand is used to restore a backup.db file in the
 * /plugins/Plan folder.
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;
    private final SQLiteDB.Factory sqliteFactory;
    private final PlanFiles files;

    @Inject
    public ManageRestoreCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            SQLiteDB.Factory sqliteFactory,
            PlanFiles files,
            ErrorHandler errorHandler
    ) {
        super("restore", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.files = files;
        this.errorHandler = errorHandler;

        setArguments("<Filename.db>", "<dbTo>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_RESTORE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_RESTORE));
    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 2, Arrays.toString(this.getArguments()))));

        String backupDbName = args[0];

        String dbName = args[1].toLowerCase();
        boolean isCorrectDB = DBType.exists(dbName);
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

        try {
            Database database = dbSystem.getActiveDatabaseByName(dbName);

            Verify.isFalse(backupDbName.contains("database") && database instanceof SQLiteDB,
                    () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));
            database.init();

            if (!Verify.contains("-a", args)) {
                sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_OVERWRITE, database.getType().getName())));
                return;
            }

            runRestoreTask(backupDbName, sender, database);
        } catch (Exception e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runRestoreTask(String backupDbName, Sender sender, Database database) {
        processing.submitCritical(() -> {
            try {
                boolean containsDBFileExtension = backupDbName.endsWith(".db");
                File backupDBFile = files.getFileFromPluginFolder(backupDbName + (containsDBFileExtension ? "" : ".db"));

                if (!backupDBFile.exists()) {
                    sender.sendMessage(locale.getString(ManageLang.FAIL_FILE_NOT_FOUND, backupDBFile.getAbsolutePath()));
                    return;
                }

                SQLiteDB backupDB = sqliteFactory.usingFile(backupDBFile);
                backupDB.init();

                Database.State dbState = database.getState();
                if (dbState != Database.State.OPEN) {
                    sender.sendMessage(locale.getString(CommandLang.WARN_DATABASE_NOT_OPEN, dbState.name()));
                }

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                database.executeTransaction(new BackupCopyTransaction(backupDB, database)).get();

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}
