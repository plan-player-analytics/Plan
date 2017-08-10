package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.plugin.command.CommandType;
import com.djrapitops.plugin.command.ISender;
import com.djrapitops.plugin.command.SubCommand;
import com.djrapitops.plugin.settings.ColorScheme;
import com.djrapitops.plugin.settings.Version;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;

/**
 * This subcommand is used to view the version and the database type in use.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public class InfoCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Subcommand Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public InfoCommand(Plan plugin) {
        super("info",
                CommandType.CONSOLE,
                Permissions.INFO.getPermission(),
                Locale.get(Msg.CMD_USG_INFO).toString());

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        ColorScheme cs = plugin.getColorScheme();
        String mColor = cs.getMainColor();
        String sColor = cs.getSecondaryColor();
        String tColor = cs.getTertiaryColor();
        String[] messages = {
                Locale.get(Msg.CMD_HEADER_INFO).toString(),
                Locale.get(Msg.CMD_CONSTANT_LIST_BALL).toString() + mColor + " Version: " + sColor + plugin.getDescription().getVersion(),
                Locale.get(Msg.CMD_CONSTANT_LIST_BALL).toString() + tColor + " " + Version.checkVersion(plugin),
                Locale.get(Msg.CMD_CONSTANT_LIST_BALL).toString() + mColor + " Active Database: " + tColor + plugin.getDB().getConfigName(),
                Locale.get(Msg.CMD_CONSTANT_FOOTER).toString()
        };
        sender.sendMessage(messages);
        return true;
    }

}
