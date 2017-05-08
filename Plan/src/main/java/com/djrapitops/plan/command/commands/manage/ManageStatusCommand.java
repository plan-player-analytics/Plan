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
 * This manage subcommand is used to check the status of the database.
 *
 * @author Rsl1122
 */
public class ManageStatusCommand extends SubCommand {

    private final Plan plugin;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ManageStatusCommand(Plan plugin) {
        super("status", Permissions.MANAGE, Phrase.CMD_USG_MANAGE_STATUS + "", CommandType.CONSOLE, "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor hColor = Phrase.COLOR_TER.color();

        sender.sendMessage(Phrase.CMD_MANAGE_STATUS_HEADER + "");

        sender.sendMessage(Phrase.CMD_MANAGE_STATUS_ACTIVE_DB.parse(plugin.getDB().getConfigName()));

        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }
}
