package main.java.com.djrapitops.plan.command.commands;

import com.djrapitops.javaplugin.command.CommandType;
import com.djrapitops.javaplugin.command.SubCommand;
import com.djrapitops.javaplugin.command.sender.ISender;
import com.djrapitops.javaplugin.utilities.VersionUtils;
import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
        super("info", CommandType.CONSOLE, Permissions.INFO.getPermission(), Phrase.CMD_USG_INFO + "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(ISender sender, String commandLabel, String[] args) {
        ChatColor tColor = Phrase.COLOR_SEC.color();

        String[] messages = {
            Phrase.CMD_INFO_HEADER + "",
            Phrase.CMD_INFO_VERSION.parse(plugin.getDescription().getVersion()),
            Phrase.CMD_BALL.toString() + tColor + " " + VersionUtils.checkVersion(plugin),
            Phrase.CMD_MANAGE_STATUS_ACTIVE_DB.parse(plugin.getDB().getConfigName()),
            Phrase.CMD_FOOTER + ""
        };
        sender.sendMessage(messages);
        return true;
    }

}
