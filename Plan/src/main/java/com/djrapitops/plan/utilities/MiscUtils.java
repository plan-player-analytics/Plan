package com.djrapitops.plan.utilities;

import com.djrapitops.plan.Phrase;
import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.DemographicsData;
import com.djrapitops.plan.data.ServerData;
import com.djrapitops.plan.data.UserData;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class MiscUtils {

    /**
     * Checks the version and returns response String.
     *
     * @return String informing about status of plugins version.
     */
    public static String checkVersion() {
        Plan plugin = getPlugin(Plan.class);
        String cVersion;
        String lineWithVersion;
        try {
            URL githubUrl = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/Plan/src/main/resources/plugin.yml");
            lineWithVersion = "";
            Scanner websiteScanner = new Scanner(githubUrl.openStream());
            while (websiteScanner.hasNextLine()) {
                String line = websiteScanner.nextLine();
                if (line.toLowerCase().contains("version")) {
                    lineWithVersion = line;
                    break;
                }
            }
            String versionString = lineWithVersion.split(": ")[1];
            int newestVersionNumber = FormatUtils.parseVersionNumber(versionString);
            cVersion = plugin.getDescription().getVersion();
            int currentVersionNumber = FormatUtils.parseVersionNumber(cVersion);
            if (newestVersionNumber > currentVersionNumber) {
                return "New Version (" + versionString + ") is availible at https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            } else {
                return "You're running the latest version";
            }
        } catch (Exception e) {
            plugin.logError("Failed to compare versions.");
        }
        return "Failed to get newest version number.";
    }

    /**
     * Used by the inspect command.
     *
     * @param args Arguments of the inspect command
     * @param sender Command sender
     * @return The name of the player searched for, if the arguments are empty
     * player's own name is returned.
     */
    public static String getPlayerDisplayname(String[] args, CommandSender sender) {
        String playerName = "";
        Plan plugin = getPlugin(Plan.class);
        if (args.length > 0) {
            if (sender.hasPermission("plan.inspect.other")
                    || !(sender instanceof Player)) {
                playerName = args[0];
            } else if (args[0].toLowerCase().equals(sender.getName().toLowerCase())) {
                playerName = sender.getName();
            } else {
                sender.sendMessage(Phrase.COMMAND_NO_PERMISSION.toString());
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

    /**
     * Get matching playernames from the offlineplayers
     *
     * @param search Part of a name to search for.
     * @return Set of OfflinePlayers that match.
     */
    public static Set<OfflinePlayer> getMatchingDisplaynames(String search) {
        List<OfflinePlayer> players = new ArrayList<>();
        players.addAll(Arrays.asList(Bukkit.getOfflinePlayers()));
        Set<OfflinePlayer> matches = new HashSet<>();
        players.parallelStream()
                .filter((OfflinePlayer player) -> (player.getName().toLowerCase().contains(search.toLowerCase())))
                .forEach((OfflinePlayer player) -> {
                    matches.add(player);
                });
        return matches;
    }

    public static List<UserData> combineUserDatas(HashMap<UUID, UserData> allFromUserData, HashMap<UUID, UserData> allToUserData, Set<UUID> uuids) {
        List<UserData> combinedData = new ArrayList<>();
        uuids.forEach((uuid) -> {
            UserData fData = allFromUserData.get(uuid);
            UserData tData = allToUserData.get(uuid);
            if (fData == null) {
                combinedData.add(tData);
            } else if (tData == null) {
                combinedData.add(fData);
            } else {
                combinedData.add(combineUserData(fData, tData));
            }
        });
        return combinedData;
    }

    private static UserData combineUserData(UserData fData, UserData tData) {
        if (fData.getLastGmSwapTime() < tData.getLastGmSwapTime()) {
            fData.setLastGmSwapTime(tData.getLastGmSwapTime());
            fData.setLastGamemode(tData.getLastGamemode());
        }
        HashMap<GameMode, Long> gmTimes = fData.getGmTimes();
        HashMap<GameMode, Long> tGmTimes = tData.getGmTimes();
        gmTimes.keySet().stream().forEach((gm) -> {
            long fTime = gmTimes.get(gm);
            if (tGmTimes.get(gm) != null) {
                long tTime = tGmTimes.get(gm);
                gmTimes.put(gm, fTime + tTime);
            }
        });
        if (fData.getLastPlayed() < tData.getLastPlayed()) {
            fData.setLastPlayed(tData.getLastPlayed());
        }
        fData.setPlayTime(fData.getPlayTime() + tData.getPlayTime());
        fData.setTimesKicked(fData.getTimesKicked() + tData.getTimesKicked());
        fData.setLoginTimes(fData.getLoginTimes() + tData.getLoginTimes());
        fData.addLocations(tData.getLocations());
        fData.addNicknames(tData.getNicknames());
        fData.addIpAddresses(tData.getIps());
        DemographicsData tDemData = tData.getDemData();
        DemographicsData fDemData = fData.getDemData();
        if (tDemData.getAge() > fDemData.getAge()) {
            fDemData.setAge(tDemData.getAge());
        }
        if (fDemData.getGeoLocation().equals("Not Known")) {
            fDemData.setGeoLocation(tDemData.getGeoLocation());
        }
        fData.setDemData(fDemData);
        return fData;
    }

    public static HashMap<Long, ServerData> combineServerDatas(HashMap<Long, ServerData> fData, HashMap<Long, ServerData> tData) {
        HashMap<Long, ServerData> combinedData = new HashMap<>();
        Set<Long> allDates = new HashSet<>();
        allDates.addAll(fData.keySet());
        allDates.addAll(tData.keySet());
        allDates.parallelStream().forEach((date) -> {
            ServerData fServerData = fData.get(date);
            ServerData tServerData = tData.get(date);
            if (fServerData == null) {
                combinedData.put(date, tServerData);
            } else if (tServerData == null) {
                combinedData.put(date, fServerData);
            } else {
                if (fServerData.getPlayersOnline() > tServerData.getPlayersOnline()) {
                    combinedData.put(date, fServerData);
                } else {
                    combinedData.put(date, tServerData);
                }
            }
        });
        return combinedData;
    }

    public static HashMap<String, Integer> combineCommandUses(HashMap<String, Integer> fData, HashMap<String, Integer> tData) {
        HashMap<String, Integer> combinedData = new HashMap<>();
        Set<String> allCommands = new HashSet<>();
        if (fData != null) {
            allCommands.addAll(fData.keySet());
        }
        if (tData != null) {
            allCommands.addAll(tData.keySet());
        }
        for (String command : allCommands) {
            boolean fDataHasCommand = false;
            if (fData != null) {
                fDataHasCommand = fData.keySet().contains(command);
            }
            boolean tDataHasCommand = false;
            if (tData != null) {
                tDataHasCommand = tData.keySet().contains(command);
            }
            int value = 0;
            if (fDataHasCommand) {
                value += fData.get(command);
            }
            if (tDataHasCommand) {
                value += tData.get(command);
            }
            combinedData.put(command, value);
        }
        return combinedData;
    }
}
