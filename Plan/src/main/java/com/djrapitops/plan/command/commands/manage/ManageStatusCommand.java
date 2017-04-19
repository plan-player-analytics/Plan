package main.java.com.djrapitops.plan.command.commands.manage;

import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rsl1122
 */
public class ManageStatusCommand extends SubCommand {

    private Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageStatusCommand(Plan plugin) {
        super("status", Permissions.MANAGE, Phrase.CMD_USG_MANAGE_STATUS + "", CommandType.CONSOLE, "");

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
        ChatColor hColor = Phrase.COLOR_TER.color();

        // Header
        sender.sendMessage(Phrase.CMD_MANAGE_STATUS_HEADER + "");

        sender.sendMessage(Phrase.CMD_MANAGE_STATUS_ACTIVE_DB.parse(plugin.getDB().getConfigName()));

        // Footer
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }
}
