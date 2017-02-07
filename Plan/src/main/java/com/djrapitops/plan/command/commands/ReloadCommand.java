package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
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
        super("reload", "plan.reload", Phrase.CMD_USG_RELOAD+"", CommandType.CONSOLE, "");

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        plugin.onDisable();
        plugin.onEnable();
        sender.sendMessage(Phrase.RELOAD_COMPLETE+"");

        return true;
    }

}
