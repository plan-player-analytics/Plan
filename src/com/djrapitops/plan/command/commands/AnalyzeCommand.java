package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AnalyzeCommand extends SubCommand {

    private Plan plugin;
    private HashMap<UUID, HashMap<String, String>> playerData;
    private HashMap<String, String> analyzedPlayerdata;
    private Date refreshDate;

    public AnalyzeCommand(Plan plugin) {
        super("analyze", "plan.analyze", "Analyze data of all players /plan analyze [-refresh]", CommandType.CONSOLE);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-refresh")) {
                if (sender.hasPermission("plan.analyze.refresh")) {
                    refreshAnalysisData(sender);
                }
            }
        }
        if (this.playerData == null || this.refreshDate == null || this.analyzedPlayerdata == null) {
            refreshAnalysisData(sender);
        }
        
        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Analysis results, refreshed " + DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + " ago:" + textColor + "] --");
        
        List<String[]> dataList = DataFormatUtils.turnDataHashMapToSortedListOfArrays(analyzedPlayerdata);
        
        sender.sendMessage("" + textColor + "Averages for " + this.playerData.size() + " player(s)");
        for (String[] dataString : dataList) {
            sender.sendMessage("" + operatorColor + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + textColor + dataString[1]);
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }

    private void refreshAnalysisData(CommandSender sender) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        sender.sendMessage(textColor + "[" + operatorColor + "Plan" + textColor + "] "
                + "Refreshing playerData, this might take a while..");
        this.playerData = DataUtils.getTotalData();
        this.refreshDate = new Date();
        this.analyzedPlayerdata = DataUtils.analyze(this.playerData);
    }

    @Deprecated
    private HashMap<String, String> analyze(HashMap<UUID, HashMap<String, String>> playerData) {
        return DataUtils.analyze(playerData);
    }
}
