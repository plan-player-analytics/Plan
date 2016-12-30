package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;
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
        boolean playerFirst = false;
        for (String arg : args) {
            if (arg.equals("-p")) {
                playerFirst = true;
            }
        }
        Set<OfflinePlayer> matchingPlayers;
        if (playerFirst) {
            String[] playername = new String[1];
            playername[0] = args[0];
            matchingPlayers = DataUtils.getMatchingDisplaynames(playername, sender, false);
        } else {
            matchingPlayers = DataUtils.getMatchingDisplaynames(args, sender, false);
        }
        args = DataFormatUtils.parseSearchArgs(args);
        HashMap<UUID, HashMap<String, DataPoint>> data = DataUtils.getTotalData(matchingPlayers);

        Date refreshDate = new Date();
        HashMap<String, List<String[]>> dataLists = new HashMap<>();
        for (UUID key : data.keySet()) {
            OfflinePlayer p = getOfflinePlayer(key);
            HashMap<String, DataPoint> dataMap = data.get(key);
            if (!dataMap.isEmpty()) {
                dataMap = DataFormatUtils.removeExtraDataPointsSearch(dataMap, args);
            }
            if (dataMap.isEmpty()) {
                dataMap.put("ERR-NO RESULTS", new DataPoint("No results were found.", DataType.OTHER));
                plugin.logToFile("SEARCH-Results\nNo results were found for: " + p.getName() + Arrays.toString(args));
            }
            dataLists.put(p.getName(), DataFormatUtils.turnDataHashMapToSortedListOfArrays(dataMap));
        }

        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;

        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Search results: took " + DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date()) + textColor + "] --");
        sender.sendMessage(operatorColor + "Results for: " + Arrays.toString(args));
        for (String playerName : dataLists.keySet()) {
            sender.sendMessage(textColor + "Matching player: " + playerName);
            for (String[] dataString : dataLists.get(playerName)) {
                sender.sendMessage("" + operatorColor + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + textColor + dataString[1]);
            }
        }
        if (dataLists.isEmpty()) {
            sender.sendMessage(operatorColor + "No results for " + textColor + Arrays.toString(args) + operatorColor + ".");
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }
}
