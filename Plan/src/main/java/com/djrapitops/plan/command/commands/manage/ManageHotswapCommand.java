package main.java.com.djrapitops.plan.command.commands.manage;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.utilities.Check;
import main.java.com.djrapitops.plan.utilities.ManageUtils;

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
        super("hotswap", CommandType.CONSOLE_WITH_ARGUMENTS, Permissions.MANAGE.getPermission(), Phrase.CMD_USG_MANAGE_HOTSWAP.toString(), "<DB>");

        this.plugin = plugin;

    }

    @Override
    public String[] addHelp() {
        ColorScheme colorScheme = Plan.getInstance().getColorScheme();

        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();

        return new String[]{
                mCol + "Manage Hotswap command",
                tCol + "  Used to change database in use on the fly.",
                sCol + "  Does not change database if connection fails"
        };
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        if (!Check.isTrue(args.length >= 1, Phrase.COMMAND_REQUIRES_ARGUMENTS_ONE.toString(), sender)) {
            return true;
        }
        String dbName = args[0].toLowerCase();
        boolean isCorrectDB = "sqlite".equals(dbName) || "mysql".equals(dbName);

        if (!Check.isTrue(isCorrectDB, Phrase.MANAGE_ERROR_INCORRECT_DB + dbName, sender)) {
            return true;
        }

        if (Check.isTrue(dbName.equals(plugin.getDB().getConfigName()), Phrase.MANAGE_ERROR_SAME_DB.toString(), sender)) {
            return true;
        }

        final Database database = ManageUtils.getDB(plugin, dbName);

        // If DB is null return
        if (!Check.isTrue(Verify.notNull(database), Phrase.MANAGE_DATABASE_FAILURE.toString(), sender)) {
            Log.error(dbName + " was null!");
            return true;
        }

        try {
            database.getVersion(); //Test db connection
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            sender.sendMessage(Phrase.MANAGE_DATABASE_FAILURE.toString());
            return true;
        }

        plugin.getConfig().set("database.type", dbName);
        plugin.saveConfig();
        plugin.onDisable();
        plugin.onEnable();
        return true;
    }
}
