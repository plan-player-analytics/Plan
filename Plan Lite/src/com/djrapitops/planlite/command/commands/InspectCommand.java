package com.djrapitops.planlite.command.commands;

import com.djrapitops.planlite.Phrase;
import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.command.CommandType;
import com.djrapitops.planlite.command.SubCommand;
import com.djrapitops.planlite.command.utils.DataFormatUtils;
import com.djrapitops.planlite.command.utils.DataUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.djrapitops.planlite.api.DataPoint;
import com.djrapitops.planlite.api.DataType;
import java.util.UUID;
import static org.bukkit.Bukkit.getOfflinePlayer;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class InspectCommand extends SubCommand {

    private PlanLite plugin;

    public InspectCommand(PlanLite plugin) {
        super("inspect", "planlite.inspect", "Inspect data /planlite <player> [-a, -r].", CommandType.CONSOLE_WITH_ARGUMENTS);

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playerName = DataUtils.getPlayerDisplayname(args, sender);
        UUID uuid;
        try {
            uuid = UUIDFetcher.getUUIDOf(playerName);
            if (uuid == null) {
                throw new Exception("Username doesn't exist.");
            }
        } catch (Exception e) {
            sender.sendMessage(Phrase.USERNAME_NOT_VALID.toString());
            return true;
        }
        OfflinePlayer p = getOfflinePlayer(uuid);
        if (!p.hasPlayedBefore()) {
            sender.sendMessage(Phrase.USERNAME_NOT_SEEN.toString());
            return true;
        }
        if (this.plugin.getHooks().isEmpty()) {
            sender.sendMessage(Phrase.ERROR_NO_HOOKS.toString());
            return true;
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

        ChatColor oColor = Phrase.COLOR_MAIN.color();
        ChatColor tColor = Phrase.COLOR_SEC.color();
        ChatColor hColor = Phrase.COLOR_TER.color();
        
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString() + oColor + " Player Analytics Lite | Inspect results: " + playerName);

        for (String[] dataString : dataList) {
            sender.sendMessage(" " + tColor + Phrase.BALL + oColor+" "+ dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + tColor + dataString[1]);
        }
        sender.sendMessage(hColor + Phrase.ARROWS_RIGHT.toString());
        return true;
    }
}
