package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * This command is used to backup a database to a .db file.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageBackupCommand extends CommandNode {

    private final Locale locale;
    private final DBSystem dbSystem;
    private SQLiteDB.Factory sqliteFactory;
    private final ErrorHandler errorHandler;

    @Inject
    public ManageBackupCommand(Locale locale, DBSystem dbSystem, SQLiteDB.Factory sqliteFactory, ErrorHandler errorHandler) {
        super("backup", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.errorHandler = errorHandler;

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_BACKUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_BACKUP));
        setArguments("<DB>");

    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        try {
            Verify.isTrue(args.length >= 1,
                    () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ONE_ARG, Arrays.toString(this.getArguments()))));

            String dbName = args[0].toLowerCase();

            boolean isCorrectDB = Verify.equalsOne(dbName, "sqlite", "mysql");
            Verify.isTrue(isCorrectDB,
                    () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

            Database database = dbSystem.getActiveDatabaseByName(dbName);
            database.init();

            runBackupTask(sender, args, database);
        } catch (DBInitException e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runBackupTask(ISender sender, String[] args, Database database) {
        Processing.submitCritical(() -> {
            try {
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));
                createNewBackup(args[0], database);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (Exception e) {
                errorHandler.log(L.ERROR, ManageBackupCommand.class, e);
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
        SQLiteDB backupDB = null;
        try {
            String timeStamp = Formatters.iso8601NoClock().apply(System::currentTimeMillis);
            String fileName = dbName + "-backup-" + timeStamp;
            backupDB = sqliteFactory.usingFileCalled(fileName);
            Collection<UUID> uuids = copyFromDB.fetch().getSavedUUIDs();
            if (uuids.isEmpty()) {
                return;
            }
            backupDB.init();
            copyFromDB.backup().backup(backupDB);
        } catch (DBException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        } finally {
            if (backupDB != null) {
                backupDB.close();
            }
        }
    }
}
