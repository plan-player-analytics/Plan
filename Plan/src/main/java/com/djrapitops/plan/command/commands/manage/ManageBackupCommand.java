package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
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
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.Sender;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

/**
 * This command is used to backup a database to a .db file.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
@Singleton
public class ManageBackupCommand extends CommandNode {

    private final Locale locale;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final SQLiteDB.Factory sqliteFactory;
    private final ErrorHandler errorHandler;

    private final Formatter<Long> iso8601LongFormatter;

    @Inject
    public ManageBackupCommand(
            Locale locale,
            Processing processing,
            DBSystem dbSystem,
            SQLiteDB.Factory sqliteFactory,
            Formatters formatters,
            ErrorHandler errorHandler
    ) {
        super("backup", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.errorHandler = errorHandler;

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

    private void runBackupTask(Sender sender, String[] args, Database database) {
        processing.submitCritical(() -> {
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
            String timeStamp = iso8601LongFormatter.apply(System.currentTimeMillis());
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
