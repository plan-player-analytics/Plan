package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.runnable.RslRunnable;
import com.djrapitops.javaplugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

/**
 * This manage subcommand is used to clear a database of all data.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageCleanCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageCleanCommand(Plan plugin) {
        super("clean", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_CLEAN + "", "<DB>");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.ifTrue(args.length != 0, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE + "", sender)) {
            return true;
        }
        String dbName = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);

        if (!Check.ifTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + dbName, sender)) {
            return true;
        }

        final Database database = ManageUtils.getDB(plugin, dbName);

        // If DB is null return
        if (!Check.ifTrue(Verify.notNull(database), Phrase.MANAGE_DATABASE_FAILURE + "", sender)) {
            Log.error(dbName + " was null!");
            return true;
        }

        runCleanTask(sender, database);
        return true;
    }

    private void runCleanTask(ISender sender, final Database database) {
        plugin.getRunnableFactory().createNew(new RslRunnable("DBCleanTask") {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                database.clean();
                sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                this.cancel();
            }
        }).runTaskAsynchronously();
    }
}
