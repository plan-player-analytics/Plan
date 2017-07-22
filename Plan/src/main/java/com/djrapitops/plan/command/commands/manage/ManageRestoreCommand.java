package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.utilities.Verify;
import java.io.File;
import java.util.Collection;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.database.databases.SQLiteDB;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

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
        super("restore", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_RESTORE + "", Phrase.ARG_RESTORE + "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 2, Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_RESTORE + ""), sender)) {
            return true;
        }
        String db = args[1].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(db) || "mysql".equals(db);

        if (!Check.isTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + db, sender)) {
            return true;
        }
        if (!Check.isTrue(Verify.contains("-a", args), Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REWRITE.parse(args[1])), sender)) {
            return true;
        }

        final Database database = ManageUtils.getDB(plugin, db);

        if (!Check.isTrue(Verify.notNull(database), Phrase.MANAGE_DATABASE_FAILURE + "", sender)) {
            Log.error(db + " was null!");
            return true;
        }

        runRestoreTask(args, sender, database);
        return true;
    }

    private void runRestoreTask(String[] args, ISender sender, final Database database) {
        plugin.getRunnableFactory().createNew(new AbsRunnable("RestoreTask") {
            @Override
            public void run() {
                try {
                    String backupDBName = args[0];
                    boolean containsDBFileExtension = backupDBName.contains(".db");

                    File backupDBFile = new File(plugin.getDataFolder(), backupDBName + (containsDBFileExtension ? "" : ".db"));
                    if (!Check.isTrue(Verify.exists(backupDBFile), Phrase.MANAGE_ERROR_BACKUP_FILE_NOT_FOUND + " " + args[0], sender)) {
                        return;
                    }

                    if (containsDBFileExtension) {
                        backupDBName = backupDBName.replace(".db", "");
                    }
                    SQLiteDB backupDB = new SQLiteDB(plugin, backupDBName);
                    if (!backupDB.init()) {
                        sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
                        return;
                    }
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    final Collection<UUID> uuids = ManageUtils.getUUIDS(backupDB);
                    if (!Check.isTrue(!Verify.isEmpty(uuids), Phrase.MANAGE_ERROR_NO_PLAYERS + " (" + backupDBName + ")", sender)) {
                        return;
                    }
                    if (ManageUtils.clearAndCopy(database, backupDB, uuids)) {
                        if (database.getConfigName().equals(plugin.getDB().getConfigName())) {
                            plugin.getHandler().getCommandUseFromDb();
                        }
                        sender.sendMessage(Phrase.MANAGE_COPY_SUCCESS.toString());
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                    }
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + " " + getTaskName(), e);
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL.toString());
                } finally {
                    this.cancel();
                }
            }
        }).runTaskAsynchronously();
    }
}
