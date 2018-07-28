package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

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

    public ManageBackupCommand(PlanPlugin plugin) {
        super("backup", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setShortHelp(locale.getString(CmdHelpLang.MANAGE_BACKUP));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_BACKUP));
        setArguments("<DB>");

    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        try {
            Verify.isTrue(args.length >= 1,
                    () -> new IllegalArgumentException(locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

            String dbName = args[0].toLowerCase();

            boolean isCorrectDB = Verify.equalsOne(dbName, "sqlite", "mysql");
            Verify.isTrue(isCorrectDB,
                    () -> new IllegalArgumentException(locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + dbName));

            Database database = DBSystem.getActiveDatabaseByName(dbName);

            Verify.nullCheck(database, NullPointerException::new);

            runBackupTask(sender, args, database);
        } catch (DBInitException | NullPointerException e) {
            sender.sendMessage(locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
    }

    private void runBackupTask(ISender sender, String[] args, Database database) {
        RunnableFactory.createNew(new AbsRunnable("BackupTask") {
            @Override
            public void run() {
                try {
                    Log.debug("Backup", "Start");
                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_START).parse());
                    createNewBackup(args[0], database);
                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_COPY_SUCCESS).toString());
                } catch (Exception e) {
                    Log.toLog(ManageBackupCommand.class, e);
                    sender.sendMessage(locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    Log.logDebug("Backup");
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
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
            backupDB = new SQLiteDB(fileName);
            Collection<UUID> uuids = copyFromDB.fetch().getSavedUUIDs();
            if (uuids.isEmpty()) {
                return;
            }
            backupDB.init();
            copyFromDB.backup().backup(backupDB);
        } catch (DBException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            if (backupDB != null) {
                backupDB.close();
            }
        }
    }
}
