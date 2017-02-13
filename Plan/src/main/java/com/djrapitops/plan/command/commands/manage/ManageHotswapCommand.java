package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.database.Database;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rsl1122
 */
public class ManageHotswapCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageHotswapCommand(Plan plugin) {
        super("hotswap", "plan.manage", "Hotswap to another database & restart the plugin", CommandType.CONSOLE_WITH_ARGUMENTS, "<DB>");

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
        String dbToSwapTo = args[0].toLowerCase();
        if (!dbToSwapTo.equals("mysql") && !dbToSwapTo.equals("sqlite")) {
            sender.sendMessage(Phrase.MANAGE_ERROR_INCORRECT_DB + dbToSwapTo);
            return true;
        }
        try {
            Database db = null;
            for (Database database : plugin.getDatabases()) {
                if (dbToSwapTo.equalsIgnoreCase(database.getConfigName())) {
                    db = database;
                    db.init();
                    db.getVersion(); //Test db connection
                }
            }
        } catch (NullPointerException e) {
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE + "");
            return true;
        }
        plugin.getConfig().set("database.type", dbToSwapTo);
        plugin.saveConfig();
        plugin.onDisable();
        plugin.onEnable();
        return true;
    }
}
