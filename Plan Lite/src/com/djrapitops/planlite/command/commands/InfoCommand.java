package com.djrapitops.planlite.command.commands;

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
        super("info", "plan.info", "View version and enabled hooks", CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        sender.sendMessage(textColor +"--["+operatorColor+"PLAN - Info"+textColor+"]--");
        sender.sendMessage(operatorColor+"Version: "+textColor+plugin.getDescription().getVersion());
        sender.sendMessage(textColor+MiscUtils.checkVersion());
        sender.sendMessage(operatorColor+"Enabled Hooks: "+textColor+plugin.getHooks().keySet());
        return true;
    }

}
