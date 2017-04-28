package main.java.com.djrapitops.plan.command.commands.manage;

import java.sql.SQLException;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import main.java.com.djrapitops.plan.database.Database;
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
        super("hotswap", Permissions.MANAGE, Phrase.CMD_USG_MANAGE_HOTSWAP + "", CommandType.CONSOLE_WITH_ARGUMENTS, "<DB>");

        this.plugin = plugin;
    }

    /**
     * Subcommand hotswap. Swaps db type and reloads plugin if the connection
     * works.
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
        } catch (NullPointerException | SQLException e) {
            plugin.toLog(this.getClass().getName(), e);
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
