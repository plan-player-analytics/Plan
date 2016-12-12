package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.UUIDFetcher;
import com.djrapitops.plan.command.hooks.AdvancedAchievementsHook;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class DataUtils {

    // allData defined by -a argument in InspectCommand
    // returns data given by each Hook
    public static HashMap<String, String> getData(boolean allData, String playerName) {
        HashMap<String, String> data = new HashMap<>();
        Plan plugin = getPlugin(Plan.class);
        plugin.getHooks().keySet().parallelStream().forEach((hook) -> {
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
        });
        return data;
    }

    // Returns data HashMaps for all pplayers in a HashMap.
    public static HashMap<UUID, HashMap<String, String>> getTotalData(Set<OfflinePlayer> ofPlayers) {
        HashMap<UUID, HashMap<String, String>> playerData = new HashMap<>();

        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(ofPlayers);
        players.parallelStream()
                .filter((player) -> (playerData.get(player.getUniqueId()) == null))
                .forEach((player) -> {
                    playerData.put(player.getUniqueId(), getData(true, player.getName()));
                });
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
        // Ignore following keys (Strings, unprocessable or irrelevant data)
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

        // Turn playerData into Hashmap of Lists sorted by keys.
        playerData.keySet().parallelStream().forEach((key) -> {
            playerData.get(key).keySet().parallelStream()
                    .filter((dataKey) -> !(ignoreKeys.contains(dataKey)))
                    .map((dataKey) -> {
                        if (playerDataLists.get(dataKey) == null) {
                            playerDataLists.put(dataKey, new ArrayList<>());
                        }
                        return dataKey;
                    })
                    .forEach((dataKey) -> {
                        playerDataLists.get(dataKey).add(playerData.get(key).get(dataKey));
                    });
        });

        // Define analysis method for keys
        String[] numbers = {"AAC-ACHIEVEMENTS", "ESS-HEALTH", "ESS-XP LEVEL", "FAC-POWER", "FAC-POWER PER HOUR",
            "FAC-POWER PER DEATH", "SVO-VOTES", "ONT-TOTAL VOTES", "ONT-TOTAL REFERRED", "ECO-BALANCE"};
        String[] booleanValues = {"ESS-BANNED", "ESS-JAILED", "ESS-MUTED", "ESS-FLYING", "TOW-ONLINE"};
        String[] timeValues = {"ONT-TOTAL PLAY"};

        List<String> numberKeys = new ArrayList<>();
        List<String> boolKeys = new ArrayList<>();
        List<String> timeKeys = new ArrayList<>();

        numberKeys.addAll(Arrays.asList(numbers));
        boolKeys.addAll(Arrays.asList(booleanValues));
        timeKeys.addAll(Arrays.asList(timeValues));

        // TODO: Add extrahook analysis methods here
        HashMap<String, String> analyzedData = new HashMap<>();
        int errors = 0;
        HashSet<String> errorTypes = new HashSet<>();

        // Analyze - Go through each key - Go through each point of data in the list.
        for (String dataKey : playerDataLists.keySet()) {
            if (numberKeys.contains(dataKey)) {
                double sum = 0;

                for (String dataPoint : playerDataLists.get(dataKey)) {
                    // Special cases separated.
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
                // Average
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
                // Average
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
                // Average
                analyzedData.put(dataKey, "" + (time * 1.0 / playerData.size()));
            }
        }
        // Log errors
        if (errors > 0) {
            String log = "ANALYZE\n" + errors + " error(s) occurred while analyzing total data.\nFollowing types:";
            for (String errorType : errorTypes) {
                log += "\n  " + errorType;
            }
            plugin.logToFile(log);
        }
        return DataFormatUtils.formatAnalyzed(analyzedData);
    }

    public static String getPlayerDisplayname(String[] args, CommandSender sender) {
        String playerName = "";
        Plan plugin = getPlugin(Plan.class);
        if (args.length > 0) {
            if ((args[0].equals("-a")) || (args[0].equals("-r"))) {
                playerName = "ArgumentGivenError";
                plugin.log("No username given, returned empty username.");

                plugin.logToFile("INSPECT-GETNAME\nNo username given, returned empty username.\n" + args[0]);

            } else if (sender.hasPermission("plan.inspect.other") || !(sender instanceof Player)) {
                playerName = args[0];
            }
        } else {
            try {
                Player player = plugin.getServer().getPlayer(UUIDFetcher.getUUIDOf(sender.getName()));
                playerName = player.getName();
            } catch (Exception e) {
                playerName = "ConsoleNotPlayerErr";
            }
        }
        return playerName;
    }

    public static Set<OfflinePlayer> getMatchingDisplaynames(String[] args, CommandSender sender, boolean all) {
        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
        Set<OfflinePlayer> matches = new HashSet<>();
        if (all) {
            matches.addAll(players);
        } else {
            List<String> searchTerms = new ArrayList<>();
            searchTerms.addAll(Arrays.asList(args));

            players.parallelStream().forEach((p) -> {
                searchTerms.stream().filter((searchTerm) -> (p.getName().toLowerCase().contains(searchTerm.toLowerCase()))).forEach((_item) -> {
                    matches.add(p);
                });
            });
        }
        return matches;
    }

    public static Set<OfflinePlayer> getMatchingDisplaynames(boolean b) {
        return getMatchingDisplaynames(new String[0], null, true);
    }
}
