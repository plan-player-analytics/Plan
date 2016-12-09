package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.command.hooks.AdvancedAchievementsHook;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class DataUtils {

    public static HashMap<String, String> getData(boolean allData, String playerName) {
        HashMap<String, String> data = new HashMap<>();
        Plan plugin = getPlugin(Plan.class);
        for (String hook : plugin.getHooks().keySet()) {
            try {
                if (allData) {
                    data.putAll(plugin.getHooks().get(hook).getAllData(playerName));
                } else {
                    data.putAll(plugin.getHooks().get(hook).getData(playerName));
                }
            } catch (Exception e) {

                String toLog = "UTILS-GetData"
                        + "\nFailed to getData from " + hook
                        + "\n" + e
                        + "\ncausing argument: " + playerName;
                for (StackTraceElement element : e.getStackTrace()) {
                    toLog += "\n  " + element;
                }
                plugin.logToFile(toLog);

            }
        }
        return data;
    }
    
    public static HashMap<UUID, HashMap<String, String>> getTotalData() {
        HashMap<UUID, HashMap<String, String>> playerData = new HashMap<>();
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (playerData.get(player.getUniqueId()) == null) {
                playerData.put(player.getUniqueId(), getData(true, player.getName()));
            } 
        }
        return playerData;
    }

    public static String[] getPlaceholdersFileData() {
        Plan plugin = getPlugin(Plan.class);
        File placeholdersFile = new File(plugin.getDataFolder(), "placeholders.yml");
        try {
            if (!placeholdersFile.exists()) {
                placeholdersFile.createNewFile();
            }
            Scanner filescanner = new Scanner(placeholdersFile);
            String placeholdersString = "";
            if (filescanner.hasNextLine()) {
                placeholdersString = filescanner.nextLine();
            }
            String[] returnArray = placeholdersString.split(" ");
            return returnArray;
        } catch (Exception e) {
            plugin.logToFile("Failed to create placeholders.yml\n" + e);
        }
        return null;
    }
    
    public static HashMap<String, String> analyze(HashMap<UUID, HashMap<String, String>> playerData) {
        Plan plugin = getPlugin(Plan.class);
        HashMap<String, List<String>> playerDataLists = new HashMap<>();
        String[] ignore = {"ESS-BAN REASON", "ESS-OPPED", "ESS-MUTE TIME", "ESS-LOCATION", "ESS-HUNGER", "ESS-LOCATION WORLD",
            "ESS-NICKNAME", "ESS-UUID", "FAC-FACTION", "ONT-LAST LOGIN", "TOW-TOWN", "TOW-REGISTERED",
            "TOW-LAST LOGIN", "TOW-OWNER OF", "TOW-PLOT PERMS", "TOW-PLOT OPTIONS", "TOW-FRIENDS", "ESS-ONLINE SINCE",
            "ESS-OFFLINE SINCE"};
        List<String> ignoreKeys = new ArrayList<>();
        try {
            AdvancedAchievementsHook aaHook = (AdvancedAchievementsHook) plugin.getHooks().get("AdvancedAchievements");
            if (!aaHook.isUsingUUID()) {
                ignoreKeys.add("AAC-ACHIEVEMENTS");
            }
        } catch (Exception e) {
            ignoreKeys.add("AAC-ACHIEVEMENTS");
        }
        ignoreKeys.addAll(Arrays.asList(ignore));
        
        for (UUID key : playerData.keySet()) {
            for (String dataKey : playerData.get(key).keySet()) {
                if (ignoreKeys.contains(dataKey)) {
                    continue;
                }
                if (playerDataLists.get(dataKey) == null) {
                    playerDataLists.put(dataKey, new ArrayList<>());
                }
                playerDataLists.get(dataKey).add(playerData.get(key).get(dataKey));
            }
        }

        String[] numbers = {"AAC-ACHIEVEMENTS","ESS-HEALTH", "ESS-XP LEVEL", "FAC-POWER", "FAC-POWER PER HOUR",
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
                        if (dataKey.equals("FAC-POWER") || dataKey.equals("AAC-ACHIEVEMENTS")) {
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
                analyzedData.put(dataKey, "" + (sum * 1.0 / playerData.size()));

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
                analyzedData.put(dataKey, "" + ((amount * 1.0 / playerData.size()) * 100) + "%");
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
                analyzedData.put(dataKey, "" + (time * 1.0 / playerData.size()));
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
