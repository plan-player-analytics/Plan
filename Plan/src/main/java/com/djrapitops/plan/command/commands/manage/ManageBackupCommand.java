package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslRunnable;
import com.djrapitops.javaplugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

/**
 * This manage subcommand is used to backup a database to a .db file.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageBackupCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageBackupCommand(Plan plugin) {
        super("backup", CommandType.CONSOLE, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_BACKUP + "", "<DB>");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        try {
            if (!Check.ifTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS.parse(Phrase.USE_BACKUP + ""), sender)) {
                return true;
            }
            String dbName = args[0].toLowerCase();
            boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);
            if (Check.ifTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + dbName, sender)) {
                return true;
            }

            final Database database = ManageUtils.getDB(plugin, dbName);

            // If DB is null return
            if (!Check.ifTrue(Verify.notNull(database), Phrase.MANAGE_DATABASE_FAILURE + "", sender)) {
                Log.error(dbName + " was null!");
                return true;
            }

            runBackupTask(sender, args, database);
        } catch (NullPointerException e) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
        }
        return true;
    }

    private void runBackupTask(ISender sender, String[] args, final Database database) {
        plugin.getRunnableFactory().createNew(new RslRunnable("BackupTask") {
            @Override
            public void run() {
                try {
                    sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                    if (ManageUtils.backup(args[0], database)) {
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
