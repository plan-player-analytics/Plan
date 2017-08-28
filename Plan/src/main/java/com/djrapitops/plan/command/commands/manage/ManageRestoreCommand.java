package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

import java.io.File;
import java.util.Collection;
import java.util.UUID;

/**
 * This manage subcommand is used to restore a backup.db file in the
 * /plugins/Plan folder.
 *
 * @author Rsl1122
 */
public class ManageRestoreCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
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
        if (!Check.isTrue(args.length >= 2, Locale.get(Msg.CMD_FAIL_REQ_ARGS).parse(this.getArguments()), sender)) {
            return true;
        }

        String db = args[1].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(db) || "mysql".equals(db);

        if (!Check.isTrue(isCorrectDB, Locale.get(Msg.MANAGE_FAIL_INCORRECT_DB) + db, sender)) {
            return true;
        }

        if (!Check.isTrue(Verify.contains("-a", args), Locale.get(Msg.MANAGE_FAIL_CONFIRM).parse(Locale.get(Msg.MANAGE_NOTIFY_REWRITE).parse(args[1])), sender)) {
            return true;
        }

        // TODO Restore command
//        final Database database = ManageUtils.getDB(plugin, db);
//
//        if (!Check.isTrue(Verify.notNull(database), Locale.get(Msg.MANAGE_FAIL_FAULTY_DB).toString(), sender)) {
//            Log.error(db + " was null!");
//            return true;
//        }
//
//        runRestoreTask(args, sender, database);
        return true;
    }

    private void runRestoreTask(String[] args, ISender sender, final Database database) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("RestoreTask") {
            @Override
            public void run() {
                try {
                    String backupDBName = args[0];
                    boolean containsDBFileExtension = backupDBName.endsWith(".db");

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (containsDBFileExtension ? "" : ".db"));
                    if (!Check.isTrue(Verify.exists(backupDBFile), Locale.get(Msg.MANAGE_FAIL_FILE_NOT_FOUND) + " " + args[0], sender)) {
                        return;
                    }

                    if (containsDBFileExtension) {
                        backupDBName = backupDBName.substring(0, backupDBName.length() - 3);
                    }

                    SQLiteDB backupDB = new SQLiteDB(plugin, backupDBName);
                    backupDB.init();

                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_START).parse());

                    final Collection<UUID> uuids = ManageUtils.getUUIDS(backupDB);
                    if (!Check.isTrue(!Verify.isEmpty(uuids), Locale.get(Msg.MANAGE_FAIL_NO_PLAYERS) + " (" + backupDBName + ")", sender)) {
                        return;
                    }

                    if (ManageUtils.clearAndCopy(database, backupDB)) {
                        if (database.getConfigName().equals(plugin.getDB().getConfigName())) {
//                            plugin.getDataCache().getCommandUseFromDb();
                        }

                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_COPY_SUCCESS).toString());
                    } else {
                        sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                    }
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + " " + getTaskName(), e);
                    sender.sendMessage(Locale.get(Msg.MANAGE_INFO_FAIL).toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
