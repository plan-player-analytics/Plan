package com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.settings.Permissions;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.Msg;
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

    public ManageRestoreCommand(PlanPlugin plugin) {
        super("restore", Permissions.MANAGE.getPermission(), CommandType.CONSOLE);
        setShortHelp(Locale.get(Msg.CMD_USG_MANAGE_RESTORE).toString());
        setArguments("<Filename.db>", "<dbTo>", "[-a]");

        this.plugin = plugin;
    }

    @Override
    public void onCommand(ISender sender, String commandLabel, String[] args) {
        Verify.isTrue(args.length >= 2,
                () -> new IllegalArgumentException(Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(Arrays.toString(this.getArguments()))));

        String db = args[1].toLowerCase();
        boolean isCorrectDB = Verify.equalsOne(db, "sqlite", "mysql");
        Verify.isTrue(isCorrectDB,
                () -> new IllegalArgumentException(Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB).toString()));

        if (!Verify.contains("-a", args)) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REWRITE).parse(args[1])));
            return;
        }

        try {
            final Database database = DBSystem.getActiveDatabaseByName(db);

            runRestoreTask(args, sender, database);
        } catch (Exception e) {
            sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString());
        }
    }

    private void runRestoreTask(String[] args, ISender sender, final Database database) {
        RunnableFactory.createNew(new AbsRunnable("RestoreTask") {
            @Override
            public void run() {
                try {
                    String backupDBName = args[0];
                    boolean containsDBFileExtension = backupDBName.endsWith(".db");

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (containsDBFileExtension ? "" : ".db"));

                    if (!backupDBFile.exists()) {
                        sender.sendMessage(Locale.get(Msg.MANAGE_FAIL_FILE_NOT_FOUND) + " " + args[0]);
                        return;
                    }

                    if (containsDBFileExtension) {
                        backupDBName = backupDBName.substring(0, backupDBName.length() - 3);
                    }

                    SQLiteDB backupDB = new SQLiteDB(backupDBName);
                    backupDB.init();

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    database.backup().restore(backupDB);

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_COPY_SUCCESS).toString());
                } catch (Exception e) {
                    Log.toLog(this.getClass(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
