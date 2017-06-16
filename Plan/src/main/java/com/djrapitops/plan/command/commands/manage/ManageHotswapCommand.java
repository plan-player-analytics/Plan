package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import java.sql.SQLException;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * This manage subcommand is used to swap to a different database and reload the
 * plugin if the connection to the new database can be established.
 *
 * @author Rsl1122
 * @since 2.3.0
 */
public class ManageHotswapCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageHotswapCommand(Plan plugin) {
        super("hotswap", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_HOTSWAP + "", "<DB>");

        this.plugin = plugin;
    }

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
            Log.toLog(this.getClass().getName(), e);
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
