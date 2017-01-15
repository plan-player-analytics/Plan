package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.Phrase;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import com.djrapitops.planlite.command.utils.DataUtils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends SubCommand {

    private PlanLite plugin;

    public ReloadCommand(PlanLite plugin) {
        super("reload", "planlite.reload", "Reload plugin config & Hooks", CommandType.CONSOLE);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        plugin.reloadConfig();
        List<String> hookFail = plugin.hookInit();
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite | Config & Hooks reloaded.");
        String loadedMsg = " Hooked into: ";
        for (String key : plugin.getHooks().keySet()) {
            loadedMsg += ChatColor.GREEN + key + " ";
        }
        String failedMsg = " Not Hooked: ";
        for (String string : hookFail) {
            failedMsg += ChatColor.RED + string + " ";
        }
        sender.sendMessage(tColor + loadedMsg);
        if (!hookFail.isEmpty()) {
            sender.sendMessage(tColor + failedMsg);
        }
        return true;
    }

}
