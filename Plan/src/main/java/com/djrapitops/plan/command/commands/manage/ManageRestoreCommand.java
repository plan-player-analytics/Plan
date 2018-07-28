package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.Msg;
import com.djrapitops.plan.system.locale.lang.CmdHelpLang;
import com.djrapitops.plan.system.locale.lang.DeepHelpLang;
import com.djrapitops.plan.system.locale.lang.ManageLang;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandNode;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;
import java.util.Arrays;

/**
 * This manage SubCommand is used to restore a backup.db file in the
 * /plugins/Plan folder.
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends CommandNode {

    private final PlanPlugin plugin;
    private final Locale locale;

    public ManageRestoreCommand(PlanPlugin plugin) {
        super("restore", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);
        this.plugin = plugin;

        locale = plugin.getSystem().getLocaleSystem().getLocale();

        setArguments("<Filename.db>", "<dbTo>", "[-a]");
        setShortHelp(locale.getString(CmdHelpLang.MANAGE_RESTORE));
        setInDepthHelp(locale.getArray(DeepHelpLang.MANAGE_RESTORE));
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

        String backupDbName = args[0];

        String dbName = args[1].toLowerCase();
        boolean isCorrectDB = Verify.equalsOne(dbName, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_INCORRECT_DB, dbName)));

        try {
            Database database = DBSystem.getActiveDatabaseByName(dbName);
            Verify.isFalse(backupDbName.contains("database") && database instanceof SQLiteDB,
                    () -> new IllegalArgumentException(locale.getString(ManageLang.FAIL_SAME_DB)));

            if (!Verify.contains("-a", args)) {
                sender.sendMessage(locale.getString(ManageLang.CONFIRMATION, locale.getString(ManageLang.CONFIRM_OVERWRITE, database.getName())));
                return;
            }

            runRestoreTask(backupDbName, sender, database);
        } catch (Exception e) {
            sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
        }
    }

    private void runRestoreTask(String backupDbName, ISender sender, final Database database) {
        RunnableFactory.createNew(new AbsRunnable("RestoreTask") {
            @Override
            public void run() {
                try {
                    String backupDBName = backupDbName;
                    boolean containsDBFileExtension = backupDBName.endsWith(".db");

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (containsDBFileExtension ? "" : ".db"));

                    if (!backupDBFile.exists()) {
                        sender.sendMessage(locale.getString(ManageLang.FAIL_FILE_NOT_FOUND, backupDBFile.getAbsolutePath()));
                        return;
                    }

                    if (containsDBFileExtension) {
                        backupDBName = backupDBName.substring(0, backupDBName.length() - 3);
                    }

                    SQLiteDB backupDB = new SQLiteDB(backupDBName);
                    backupDB.init();

                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_START));

                    database.backup().restore(backupDB);

                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_SUCCESS));
                } catch (Exception e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(locale.getString(ManageLang.PROGRESS_FAIL, e.getMessage()));
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
