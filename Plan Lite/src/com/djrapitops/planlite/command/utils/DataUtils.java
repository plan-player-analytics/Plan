package com.djrapitops.planlite.command.utils;

import com.djrapitops.planlite.PlanLite;
import com.djrapitops.planlite.UUIDFetcher;
import com.djrapitops.planlite.api.DataPoint;
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
    public static HashMap<String, DataPoint> getData(boolean allData, String playerName) {
        HashMap<String, DataPoint> data = new HashMap<>();
        PlanLite plugin = getPlugin(PlanLite.class);
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
    public static HashMap<UUID, HashMap<String, DataPoint>> getTotalData(Set<OfflinePlayer> ofPlayers) {
        HashMap<UUID, HashMap<String, DataPoint>> playerData = new HashMap<>();

        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(ofPlayers);
        players.parallelStream()
                .filter((player) -> (playerData.get(player.getUniqueId()) == null))
                .forEach((player) -> {
                    playerData.put(player.getUniqueId(), getData(true, player.getName()));
                });
        return playerData;
    }

    @Deprecated
    public static HashMap<String, DataPoint> analyze(HashMap<UUID, HashMap<String, DataPoint>> playerData) {        
        return Analysis.analyze(playerData);
    }

    public static String getPlayerDisplayname(String[] args, CommandSender sender) {
        String playerName = "";
        PlanLite plugin = getPlugin(PlanLite.class);
        if (args.length > 0) {
            if ((args[0].equals("-a")) || (args[0].equals("-r"))) {
                playerName = "ArgumentGivenError";
                plugin.log("No username given, returned empty username.");

                plugin.logToFile("INSPECT-GETNAME\nNo username given, returned empty username.\n" + args[0]);

            } else if (sender.hasPermission("planlite.inspect.other") || !(sender instanceof Player)) {
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
