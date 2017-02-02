package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;

import com.djrapitops.plan.database.Database;
import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 *
 * @author Rsl1122
 */
public class ManageClearCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageClearCommand(Plan plugin) {
        super("clear", "plan.manage", "Clear data from one database", CommandType.CONSOLE_WITH_ARGUMENTS, "<DB> [-a]");

        this.plugin = plugin;
    }

    /**
     * Subcommand inspect.
     *
     * Adds player's data from DataCache/DB to the InspectCache for amount of
     * time specified in the config, and clears the data from Cache with a timer
     * task.
     *
     * @param sender
     * @param cmd
     * @param commandLabel
     * @param args Player's name or nothing - if empty sender's name is used.
     * @return true in all cases.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
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
            sender.sendMessage(Phrase.COMMAND_ADD_CONFIRMATION_ARGUMENT.toString() + " Data in " + args[0] + "-database will be removed!");
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
            plugin.logError(dbToClear + " was null!");
            return true;
        }

        final Database clearThisDB = clearDB;
        (new BukkitRunnable() {
            @Override
            public void run() {
                sender.sendMessage(Phrase.MANAGE_PROCESS_START.parse());
                clearThisDB.removeAllData();
                sender.sendMessage(Phrase.MANAGE_CLEAR_SUCCESS+"");
                this.cancel();
            }
        }).runTaskAsynchronously(plugin);
        return true;
    }
}
