package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.file.FileSystem;
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
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;
    private SQLiteDB.Factory sqliteFactory;
    private final FileSystem fileSystem;

    @Inject
    public ManageRestoreCommand(Locale locale, DBSystem dbSystem, SQLiteDB.Factory sqliteFactory, FileSystem fileSystem, ErrorHandler errorHandler) {
        super("restore", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        this.locale = locale;
        this.dbSystem = dbSystem;
        this.sqliteFactory = sqliteFactory;
        this.fileSystem = fileSystem;
        this.errorHandler = errorHandler;

        setArguments("<Filename.db>", "<dbTo>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_RESTORE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_RESTORE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(locale.getString(CommandLang.FAIL_REQ_ARGS, 2, Arrays.toString(this.getArguments()))));

        String backupDbName = args[0];

        String dbName = args[1].toLowerCase();
        boolean isCorrectDB = Verify.equalsOne(dbName, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

        try {
            Database database = dbSystem.getActiveDatabaseByName(dbName);

            Verify.isFalse(backupDbName.contains("database") && database instanceof SQLiteDB,
                    () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));
            database.init();

            if (!Verify.contains("-a", args)) {
                sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_OVERWRITE, database.getName())));
                return;
            }

            runRestoreTask(backupDbName, sender, database);
        } catch (Exception e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runRestoreTask(String backupDbName, ISender sender, Database database) {
        Processing.submitCritical(() -> {
            try {
                String backupDBName = backupDbName;
                boolean containsDBFileExtension = backupDBName.endsWith(".db");

                File backupDBFile = fileSystem.getFileFromPluginFolder(backupDBName + (containsDBFileExtension ? "" : ".db"));

                if (!backupDBFile.exists()) {
                    sender.sendMessage(locale.getString(ManageLang.FAIL_FILE_NOT_FOUND, backupDBFile.getAbsolutePath()));
                    return;
                }

                if (containsDBFileExtension) {
                    backupDBName = backupDBName.substring(0, backupDBName.length() - 3);
                }

                SQLiteDB backupDB = sqliteFactory.usingFile(backupDBFile);
                backupDB.init();

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                database.backup().restore(backupDB);

                sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
            } catch (Exception e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
                sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
            }
        });
    }
}
