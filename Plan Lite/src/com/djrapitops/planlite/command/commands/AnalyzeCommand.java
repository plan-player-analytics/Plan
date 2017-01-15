package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.Phrase;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.command.utils.DataFormatUtils;
import com.djrapitops.planlite.command.utils.DataUtils;
import com.djrapitops.planlite.command.utils.Analysis;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AnalyzeCommand extends SubCommand {

    private PlanLite plugin;
    private HashMap<UUID, HashMap<String, DataPoint>> playerData;
    private HashMap<String, DataPoint> analyzedPlayerdata;
    private Date refreshDate;

    public AnalyzeCommand(PlanLite plugin) {
        super("analyze", "planlite.analyze", "Analyze data of players, /planlite analyze [-refresh]", CommandType.CONSOLE);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        for (String arg : args) {
            if (arg.toLowerCase().equals("-refresh")) {
                if (sender.hasPermission("planlite.analyze.refresh") || !(sender instanceof Player)) {
                    refreshAnalysisData(sender);
                } else {
                    sender.sendMessage(Phrase.COMMAND_NO_PERMISSION.toString());
                }
            }
        }
        if (this.playerData == null || this.refreshDate == null || this.analyzedPlayerdata == null || DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()).contains("m")) {
            refreshAnalysisData(sender);
        }

        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite | Analysis results - refreshed "
                + DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + " ago");

        List<String[]> dataList = DataFormatUtils.turnDataHashMapToSortedListOfArrays(analyzedPlayerdata);

        sender.sendMessage(hColor + Phrase.BALL.toString() + tColor + " Averages for " + hColor + this.playerData.size() + tColor + " player(s)");
        for (String[] dataString : dataList) {
            sender.sendMessage(" " + tColor + Phrase.BALL + oColor+" "
                    + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + tColor + dataString[1]);
        }
        sender.sendMessage(hColor + Phrase.BALL.toString());
        return true;
    }

    private void refreshAnalysisData(CommandSender sender) {
        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite " + tColor + "| "
                + "Refreshing playerData, this might take a while..");
        this.playerData = DataUtils.getTotalData(DataUtils.getMatchingDisplaynames(true));
        this.refreshDate = new Date();
        this.analyzedPlayerdata = Analysis.analyze(this.playerData);
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite " + tColor + "| "
                + "Refreshed, took " + DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()));
    }
}
