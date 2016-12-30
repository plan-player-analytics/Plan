package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.PlanLite;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.djrapitops.plan.api.DataPoint;
import com.djrapitops.plan.api.DataType;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InspectCommand extends SubCommand {

    private PlanLite plugin;

    public InspectCommand(PlanLite plugin) {
        super("inspect", "plan.inspect", "Inspect data /plan <player> [-a, -r].", CommandType.CONSOLE_WITH_ARGUMENTS);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = DataUtils.getPlayerDisplayname(args, sender);
        if (this.plugin.getHooks().isEmpty()) {
            this.plugin.logError("noHookedPluginsError on InspectCommand");

            this.plugin.logToFile("INSPECT\nnoHookedPluginsError on InspectCommand");

            return false;
        }

        boolean allData = false;
        boolean format = true;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-a")) {
                allData = true;
            }
            if (arg.toLowerCase().equals("-r")) {
                format = false;
            }
        }
        Date refreshDate = new Date();
        HashMap<String, DataPoint> data = DataUtils.getData(allData, playerName);
        if (format && !data.isEmpty()) {
            data = DataFormatUtils.removeExtraDataPoints(data);
        }
        if (data.isEmpty()) {
            data.put("ERR-NO RESULTS", new DataPoint("No results were found.", DataType.OTHER));

            plugin.logToFile("INSPECT-Results\nNo results were found for: " + playerName);

        }

        List<String[]> dataList = DataFormatUtils.turnDataHashMapToSortedListOfArrays(data);

        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;

        //header
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Inspect results: " + playerName +" - took "+DataFormatUtils.formatTimeAmountSinceDate(refreshDate, new Date())+ textColor + "] --");

        for (String[] dataString : dataList) {
            sender.sendMessage("" + operatorColor + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + textColor + dataString[1]);
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }
}
