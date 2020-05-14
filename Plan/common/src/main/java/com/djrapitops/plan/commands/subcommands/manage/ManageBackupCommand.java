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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.settings.Permissions;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.CmdHelpLang;
import com.djrapitops.plan.settings.locale.lang.CommandLang;
import com.djrapitops.plan.settings.locale.lang.DeepHelpLang;
import com.djrapitops.plan.settings.locale.lang.ManageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.DBType;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.SQLiteDB;
import com.djrapitops.plan.storage.database.queries.ServerAggregateQueries;
import com.djrapitops.plan.storage.database.transactions.BackupCopyTransaction;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

/**
 * This command is used to backup a database to a .db file.
 *
 * @author Rsl1122
 */
@Singleton
public class ManageBackupCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final SQLiteDB.Factory sqliteFactory;
    private final ErrorLogger errorLogger;

    private final Formatter<Long> iso8601LongFormatter;

    @Inject
    public ManageBackupCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            SQLiteDB.Factory sqliteFactory,
            Formatters formatters,
            ErrorLogger errorLogger
    ) {
        super("backup", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.errorLogger = errorLogger;

        this.iso8601LongFormatter = formatters.iso8601NoClockLong();

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_BACKUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_BACKUP));
        setArguments("<DB>");

    }

    @Override
    public void onCommand(Sender sender, String commandLabel, String[] args) {
        try {
            Verify.isTrue(args.length >= 1,
                    () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

            String dbName = args[0].toLowerCase();

            boolean isCorrectDB = DBType.exists(dbName);
            Verify.isTrue(isCorrectDB,
                    () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

            Database database = dbSystem.getActiveDatabaseByName(dbName);
            database.init();

            runBackupTask(sender, args, database);
        } catch (DBInitException e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runBackupTask(Sender sender, String[] args, Database database) {
        processing.submitCritical(() -> {
            try {
                Database.State dbState = database.getState();
                if (dbState != Database.State.OPEN) {
                    sender.sendMessage(locale.getString(CommandLang.WARN_DATABASE_NOT_OPEN, dbState.name()));
                }
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));
                createNewBackup(args[0], database);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (Exception e) {
                errorLogger.log(L.ERROR, ManageBackupCommand.class, e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }

    /**
     * Creates a new backup sqlite file with the data of given database.
     *
     * @param dbName     Name of database (mysql/sqlite)
     * @param copyFromDB Database you want to backup.
     */
    private void createNewBackup(String dbName, Database copyFromDB) {
        Integer userCount = copyFromDB.query(ServerAggregateQueries.baseUserCount());
        if (userCount <= 0) {
            return;
        }
        Database backupDB = null;
        try {
            String timeStamp = iso8601LongFormatter.apply(System.currentTimeMillis());
            String fileName = dbName + "-backup-" + timeStamp;
            backupDB = sqliteFactory.usingFileCalled(fileName);
            backupDB.init();
            backupDB.executeTransaction(new BackupCopyTransaction(copyFromDB, backupDB)).get();
        } catch (DBOpException | ExecutionException e) {
            errorLogger.log(L.ERROR, this.getClass(), e);
        } catch (InterruptedException e) {
            backupDB.close();
            Thread.currentThread().interrupt();
        } finally {
            if (backupDB != null) {
                backupDB.close();
            }
        }
    }
}
