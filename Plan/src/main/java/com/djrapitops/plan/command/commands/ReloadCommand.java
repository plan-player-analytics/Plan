package main.java.com.djrapitops.plan.command.commands;

import main.java.com.djrapitops.plan.Permissions;
import main.java.com.djrapitops.plan.Phrase;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.command.CommandType;
import main.java.com.djrapitops.plan.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 *
 * @author Rsl1122
 */
public class ReloadCommand extends SubCommand {

    private Plan plugin;

    /**
     * Subcommand constructor.
     *
     * @param plugin Current instance of Plan
     */
    public ReloadCommand(Plan plugin) {
        super("reload", Permissions.MANAGE, Phrase.CMD_USG_RELOAD + "", CommandType.CONSOLE, "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
        sender.sendMessage(Phrase.RELOAD_COMPLETE + "");

        return true;
    }

}
