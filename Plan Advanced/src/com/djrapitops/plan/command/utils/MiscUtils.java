package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.bukkit.GameMode;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class MiscUtils {
//    <h1>Plan - Player Analytics <span class="muted">

    public static String checkVersion() {
        Plan plugin = getPlugin(Plan.class);
        String[] nVersion;
        String[] cVersion;
        String lineWithVersion;
        try {
            URL githubUrl = new URL("https://raw.githubusercontent.com/Rsl1122/Plan-PlayerAnalytics/master/src/plugin.yml");
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
            nVersion = versionString.split("\\.");
            double newestVersionNumber = Double.parseDouble(nVersion[0] + "." + nVersion[1] + nVersion[2]);
            cVersion = plugin.getDescription().getVersion().split("\\.");
            double currentVersionNumber = Double.parseDouble(cVersion[0] + "." + cVersion[1] + cVersion[2]);
            if (newestVersionNumber > currentVersionNumber) {
                return "New Version (" + versionString + ") is availible at https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            } else {
                return "You're running the latest version";
            }
        } catch (Exception e) {
            plugin.logToFile("Failed to compare versions.\n"+e);
        }
        return "Failed to get newest version number.";
    }

    public static String[] mergeArrays(String[]... arrays) {
        int arraySize = 0;
        for (String[] array : arrays) {
            arraySize += array.length;
        }
        String[] result = new String[arraySize];
        int j = 0;
        for (String[] array : arrays) {
            for (String string : array) {
                result[j++] = string;
            }
        }
        return result;
    }

    public static GameMode parseGM(String string) {
        String survival = GameMode.SURVIVAL.name();
        String creative = GameMode.CREATIVE.name();
        String adventure = GameMode.ADVENTURE.name();
        String spectator = GameMode.SPECTATOR.name();
        if (string.equalsIgnoreCase(survival)) {
            return GameMode.SURVIVAL;
        } else if (string.equalsIgnoreCase(creative)) {
            return GameMode.CREATIVE;
        } else if (string.equalsIgnoreCase(adventure)) {
            return GameMode.ADVENTURE;
        } else if (string.equalsIgnoreCase(spectator)) {
            return GameMode.SPECTATOR;
        } else {
            return GameMode.SURVIVAL;
        }
    }
}
