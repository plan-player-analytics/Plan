package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SearchCommand extends SubCommand {

    private final Plan plugin;

    public SearchCommand(Plan plugin) {
        super("search", "plan.search", "Inspect specific data /plan <search terms> [-p]", CommandType.CONSOLE_WITH_ARGUMENTS);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        
        Date refreshDate = new Date();

        ChatColor operatorColor = Phrase.COLOR_MAIN.color();
        ChatColor textColor = Phrase.COLOR_SEC.color();

        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Search results: took " + FormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + textColor + "] --");
        sender.sendMessage(operatorColor + "Results for: " + Arrays.toString(args));

        sender.sendMessage(textColor + "Matching player: ");

        if (false) {
            sender.sendMessage(operatorColor + "No results for " + textColor + Arrays.toString(args) + operatorColor + ".");
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }
}
