package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import com.djrapitops.javaplugin.task.RslRunnable;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        if (args.length == 0) {
            sender.sendMessage(Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE + "");
            return true;
        }
        String dbToClear = args[0].toLowerCase();
        if (!dbToClear.equals("mysql") && !dbToClear.equals("sqlite")) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + dbToClear);
            return true;
        }

        Database clearDB = null;
        for (Database database : plugin.getDatabases()) {
            if (dbToClear.equalsIgnoreCase(database.getConfigName())) {
                clearDB = database;
                clearDB.init();
            }
        }
        if (clearDB == null) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
            Log.error(dbToClear + " was null!");
            return true;
        }

        final Database clearThisDB = clearDB;
        plugin.getRunnableFactory().createNew(new RslRunnable("DBCleanTask") {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                clearThisDB.clean();
                sender.sendMessage(Phrase.MANAGE_SUCCESS + "");
                this.cancel();
            }
        }).runTaskAsynchronously();
        return true;
    }
}
