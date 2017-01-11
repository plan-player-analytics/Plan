package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.utilities.FormatUtils;
import java.util.Date;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AnalyzeCommand extends SubCommand {

    private Plan plugin;
    private Date refreshDate;

    public AnalyzeCommand(Plan plugin) {
        super("analyze", "plan.analyze", "Analyze data of all players /plan analyze [-refresh]", CommandType.CONSOLE);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        
        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Analysis results, refreshed " 
                + FormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + " ago:" + textColor + "] --");
        
        sender.sendMessage(textColor + "-- o --");
        return true;
    }
}
