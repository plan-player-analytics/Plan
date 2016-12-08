package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import java.io.File;
import java.util.HashMap;
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
}
