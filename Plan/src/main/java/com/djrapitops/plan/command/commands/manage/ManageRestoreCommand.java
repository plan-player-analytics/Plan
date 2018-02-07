package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
import com.djrapitops.plan.utilities.Condition;
import com.djrapitops.plan.utilities.ManageUtils;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;

import java.io.File;

/**
 * This manage SubCommand is used to restore a backup.db file in the
 * /plugins/Plan folder.
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends SubCommand {

    private final Plan plugin;

    public ManageRestoreCommand(Plan plugin) {
        super("restore",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                Locale.get(Msg.CMD_USG_MANAGE_RESTORE).toString(),
                "<Filename.db> <dbTo> [-a]");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Condition.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return true;
        }

        String db = args[1].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(db) || "mysql".equals(db);

        if (!Condition.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + db, sender)) {
            return true;
        }

        if (!Condition.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REWRITE).parse(args[1])), sender)) {
            return true;
        }

        try {
            final Database database = DBSystem.getActiveDatabaseByName(db);

            runRestoreTask(args, sender, database);
        } catch (Exception e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
        return true;
    }

    private void runRestoreTask(String[] args, ISender sender, final Database database) {
        RunnableFactory.createNew(new AbsRunnable("RestoreTask") {
            @Override
            public void run() {
                try {
                    String backupDBName = args[0];
                    boolean containsDBFileExtension = backupDBName.endsWith(".db");

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (containsDBFileExtension ? "" : ".db"));
                    if (!Condition.isTrue(Verify.exists(backupDBFile), Locale.get(Msg.MANAGE_FAIL_FILE_NOT_FOUND) + " " + args[0], sender)) {
                        return;
                    }

                    if (containsDBFileExtension) {
                        backupDBName = backupDBName.substring(0, backupDBName.length() - 3);
                    }

                    SQLiteDB backupDB = new SQLiteDB(backupDBName);
                    backupDB.init();

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    ManageUtils.clearAndCopy(database, backupDB);

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_COPY_SUCCESS).toString());
                } catch (Exception e) {
                    Log.toLog(this.getClass() + " " + getTaskName(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
