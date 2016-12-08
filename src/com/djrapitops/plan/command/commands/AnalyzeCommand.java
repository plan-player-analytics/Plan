package com.djrapitops.plan.command.commands;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.CommandType;
import com.djrapitops.plan.command.SubCommand;
import com.djrapitops.plan.command.utils.DataFormatUtils;
import com.djrapitops.plan.command.utils.DataUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
//        this.plugin.log("Refreshing playerDataMap, this might take a while..");
//        this.playerData = DataUtils.getTotalData();
//        this.analyzedPlayerdata = analyze(this.playerData);
//        this.refreshDate = new Date();
//        this.plugin.log("PlayerDataMap refresh complete.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        ChatColor operatorColor = ChatColor.DARK_GREEN;
        ChatColor textColor = ChatColor.GRAY;
        for (String arg : args) {
            if (arg.toLowerCase().equals("-refresh")) {
                if (sender.hasPermission("plan.analyze.refresh")) {
                    sender.sendMessage(textColor + "[" + operatorColor + "Plan" + textColor + "] "
                            + "Refreshing playerData, this might take a while..");
                    this.playerData = DataUtils.getTotalData();
                    this.refreshDate = new Date();
                    this.analyzedPlayerdata = analyze(this.playerData);

                }
            }
        }
        if (this.playerData == null || this.refreshDate == null || this.analyzedPlayerdata == null) {
            sender.sendMessage(textColor + "[" + operatorColor + "Plan" + textColor + "] "
                    + "Refreshing playerData, this might take a while..");
            this.playerData = DataUtils.getTotalData();
            this.refreshDate = new Date();
            this.analyzedPlayerdata = analyze(this.playerData);
        }
        List<String[]> dataList = new ArrayList<>();
        sender.sendMessage(textColor + "-- [" + operatorColor + "PLAN - Analysis results, refreshed " + DataFormatUtils.formatTimeAmount(refreshDate, new Date()) + " ago:" + textColor + "] --");
        for (String key : this.analyzedPlayerdata.keySet()) {
            dataList.add(new String[]{key, this.analyzedPlayerdata.get(key)});
        }
        Collections.sort(dataList, new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[0].compareTo(otherStrings[0]);
            }
        });
        sender.sendMessage("" + textColor + "Averages for "+this.playerData.size()+" player(s)");
        for (String[] dataString : dataList) {
            sender.sendMessage("" + operatorColor + dataString[0].charAt(4) + dataString[0].toLowerCase().substring(5) + ": " + textColor + dataString[1]);
        }
        sender.sendMessage(textColor + "-- o --");
        return true;
    }

    private HashMap<String, String> analyze(HashMap<UUID, HashMap<String, String>> playerData) {
        HashMap<String, List<String>> playerDataLists = new HashMap<>();
        String[] ignore = {"ESS-BAN REASON", "ESS-OPPED", "ESS-MUTE TIME", "ESS-LOCATION", "ESS-HUNGER", "ESS-LOCATION WORLD",
            "ESS-NICKNAME", "ESS-UUID", "FAC-FACTION", "ONT-LAST LOGIN", "TOW-TOWN", "TOW-REGISTERED",
            "TOW-LAST LOGIN", "TOW-OWNER OF", "TOW-PLOT PERMS", "TOW-PLOT OPTIONS", "TOW-FRIENDS", "ESS-ONLINE SINCE",
            "ESS-OFFLINE SINCE"};
        List<String> ignoreKeys = new ArrayList<>();
        ignoreKeys.addAll(Arrays.asList(ignore));
        for (UUID key : playerData.keySet()) {
            for (String dataKey : playerData.get(key).keySet()) {
                if (!ignoreKeys.contains(dataKey)) {
                    if (playerDataLists.get(dataKey) == null) {
                        playerDataLists.put(dataKey, new ArrayList<>());
                    }
                    playerDataLists.get(dataKey).add(playerData.get(key).get(dataKey));
                }
            }
        }

        String[] numbers = {"ESS-HEALTH", "ESS-XP LEVEL", "FAC-POWER", "FAC-POWER PER HOUR",
            "FAC-POWER PER DEATH", "SVO-VOTES", "ONT-TOTAL VOTES", "ONT-TOTAL REFERRED", "ECO-BALANCE"};
        List<String> numberKeys = new ArrayList<>();
        numberKeys.addAll(Arrays.asList(numbers));
        String[] booleanValues = {"ESS-BANNED", "ESS-JAILED", "ESS-MUTED", "ESS-FLYING", "TOW-ONLINE"};
        List<String> boolKeys = new ArrayList<>();
        boolKeys.addAll(Arrays.asList(booleanValues));
        String[] timeValues = {"ONT-TOTAL PLAY"};
        List<String> timeKeys = new ArrayList<>();
        timeKeys.addAll(Arrays.asList(timeValues));

        HashMap<String, String> analyzedData = new HashMap<>();
        int errors = 0;
        HashSet<String> errorTypes = new HashSet<>();

        for (String dataKey : playerDataLists.keySet()) {
            if (numberKeys.contains(dataKey)) {
                double sum = 0;

                for (String dataPoint : playerDataLists.get(dataKey)) {
                    try {
                        if (dataKey.equals("FAC-POWER")) {
                            sum += Double.parseDouble(dataPoint.split(" ")[0]);
                        } else if (dataKey.equals("ECO-BALANCE")) {
                            sum += Double.parseDouble(DataFormatUtils.removeLetters(dataPoint));
                        } else {
                            sum += Double.parseDouble(dataPoint);
                        }
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                analyzedData.put(dataKey, "" + (sum / this.playerData.size()));

            } else if (boolKeys.contains(dataKey)) {
                int amount = 0;
                for (String dataPoint : playerDataLists.get(dataKey)) {
                    try {
                        if (Boolean.parseBoolean(dataPoint)) {
                            amount++;
                        }
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                analyzedData.put(dataKey, "" + ((amount / this.playerData.size())*100) + "%");
            } else if (timeKeys.contains(dataKey)) {
                Long time = Long.parseLong("0");
                for (String dataPoint : playerDataLists.get(dataKey)) {
                    try {
                        time += Long.parseLong(dataPoint);
                    } catch (Exception e) {
                        errors++;
                        errorTypes.add("" + e);
                    }
                }
                analyzedData.put(dataKey, "" + (time / this.playerData.size()));
            }
        }
        if (errors > 0) {
            String log = "ANALYZE\n" + errors + " error(s) occurred while analyzing total data.\nFollowing types:";
            for (String errorType : errorTypes) {
                log += "\n  " + errorType;
            }
            plugin.logToFile(log);
        }
        return DataFormatUtils.formatAnalyzed(analyzedData);
    }

}
