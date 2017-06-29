package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.task.RslBukkitRunnable;
import java.sql.SQLException;
import java.util.Arrays;
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
public class ManageClearCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageClearCommand(Plan plugin) {
        super("clear", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_CLEAR + "", "<DB> [-a]");

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
        if (!Arrays.asList(args).contains("-a")) {
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.parse(Phrase.WARN_REMOVE.parse(args[0])));
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
        (new RslBukkitRunnable<Plan>("DBClearTask") {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                try {
                    if (clearThisDB.removeAllData()) {
                        sender.sendMessage(Phrase.MANAGE_CLEAR_SUCCESS + "");
                    } else {
                        sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                    }
                } catch (SQLException e) {
                    Log.toLog(this.getClass().getName(), e);
                    sender.sendMessage(Phrase.MANAGE_PROCESS_FAIL + "");
                }
                this.cancel();
            }
        }).runTaskAsynchronously();
        return true;
    }
}
