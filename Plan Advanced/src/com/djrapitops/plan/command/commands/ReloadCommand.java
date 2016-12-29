package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.hooks.PlaceholderAPIHook;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private Plan plugin;

    public ReloadCommand(Plan plugin) {
        super("reload", "plan.reload", "Reload plugin config & Hooks", CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        List<String> hookFail = plugin.hookInit();
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        sender.sendMessage(textColor + "[" + operatorColor + "PLAN" + textColor + "] Config & Hooks reloaded.");
        String loadedMsg = " Hooked into: ";
        for (String key : plugin.getHooks().keySet()) {
            loadedMsg += ChatColor.GREEN + key + " ";
        }
        String failedMsg = " Not Hooked: ";
        for (String string : hookFail) {
            failedMsg += ChatColor.RED + string + " ";
        }
        sender.sendMessage(textColor + loadedMsg);
        if (!hookFail.isEmpty()) {
            sender.sendMessage(textColor + failedMsg);
        }
        return true;
    }

}
