package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.utils.MiscUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InfoCommand extends SubCommand {

    private Plan plugin;

    public InfoCommand(Plan plugin) {
        super("info", "plan.info", "View version and enabled hooks", CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        sender.sendMessage(tColor +"--["+oColor+"PLAN - Info"+tColor+"]--");
        sender.sendMessage(oColor+"Version: "+tColor+plugin.getDescription().getVersion());
        sender.sendMessage(tColor+MiscUtils.checkVersion());
        sender.sendMessage(oColor+"Cache\n"+tColor+"Cached users: "+plugin.getHandler().getDataCache().keySet().size());
        return true;
    }

}
