package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.api.IPlan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

import java.sql.SQLException;
import java.util.Map;

/**
 * This subcommand is used to reload the plugin.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class ListServersCommand extends SubCommand {

    private final IPlan plugin;

    /**
     * Subcommand constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ListServersCommand(Plan plugin) {
        super("servers, serverlist, listservers, sl",
                CommandType.CONSOLE,
                Permissions.MANAGE.getPermission(),
                "List servers in the network");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme colorScheme = plugin.getColorScheme();
        String mCol = colorScheme.getMainColor();
        String sCol = colorScheme.getSecondaryColor();
        String tCol = colorScheme.getTertiaryColor();
        try {
            sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString() + mCol + " Servers");
            Map<Integer, String> serverNames = plugin.getDB().getServerTable().getServerNames();
            for (Map.Entry<Integer, String> entry : serverNames.entrySet()) {
                sender.sendMessage("  " + tCol + entry.getKey() + sCol + " : " + entry.getValue());
            }
            sender.sendMessage(Locale.get(Msg.CMD_CONSTANT_FOOTER).toString());
        } catch (SQLException e) {
            sender.sendMessage("§cSQLException occurred.");
            Log.toLog(this.getClass().getName(), e);
        }
        return true;
    }

}
