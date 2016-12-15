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

    @Deprecated
    public static HashMap<String, String> analyze(HashMap<UUID, HashMap<String, String>> playerData) {        
        return Analysis.analyze(playerData);
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
