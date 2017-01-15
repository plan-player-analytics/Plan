package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.Phrase;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import com.djrapitops.planlite.command.utils.MiscUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InfoCommand extends SubCommand {

    private PlanLite plugin;

    public InfoCommand(PlanLite plugin) {
        super("info", "planlite.info", "View version and enabled hooks", CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite | Info");
        sender.sendMessage(oColor+"Version: "+tColor+plugin.getDescription().getVersion());
        sender.sendMessage(tColor+MiscUtils.checkVersion());
        sender.sendMessage(oColor+"Enabled Hooks: "+tColor+plugin.getHooks().keySet());
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }

}
