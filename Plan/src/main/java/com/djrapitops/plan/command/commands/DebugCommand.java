package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DebugCommand extends SubCommand {

    private Plan plugin;

    public DebugCommand(Plan plugin) {
        super("debug", "plan.debug", "Test plugin for possible errors (debug feature)", CommandType.PLAYER);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!plugin.getConfig().getBoolean("debug")) {
            sender.sendMessage(ChatColor.RED+"[Plan] Debug disabled in config");
            return true;
        }
        String[] commands = {"plan", "plan info", "plan reload", "plan inspect", 
            "plan inspect "+sender.getName()+"-a", "plan inspect reinogiern", 
            "plan analyze", "plan search", "plan search "+sender.getName()+" -p"};
        for (String command : commands) {
            Bukkit.dispatchCommand(sender, command);
        }
        sender.sendMessage(ChatColor.GREEN+"[Plan] Debug successful, possible errors written in file.");
        return true;
    }

}
