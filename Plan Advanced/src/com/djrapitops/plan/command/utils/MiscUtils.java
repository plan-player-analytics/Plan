package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import java.net.URL;
import java.util.Scanner;
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
            double newestVersionNumber = parseVersionDouble(versionString);
            cVersion = plugin.getDescription().getVersion();
            double currentVersionNumber = parseVersionDouble(cVersion);
            if (newestVersionNumber > currentVersionNumber) {
                return "New Version (" + versionString + ") is availible at https://www.spigotmc.org/resources/plan-player-analytics.32536/";
            } else {
                return "You're running the latest version";
            }
        } catch (Exception e) {
            plugin.logToFile("Failed to compare versions.\n" + e);
        }
        return "Failed to get newest version number.";
    }

    /**
     * Turns the version string into a double
     *
     * @param versionString String - number format 1.1.1
     * @return parsed double - for example 1,11
     * @throws NumberFormatException When wrong format
     */
    public static double parseVersionDouble(String versionString) throws NumberFormatException {
        String[] versionArray = versionString.split("\\.");
        if (versionArray.length != 3) {
            throw new NumberFormatException("Wrong format used");
        }
        double versionDouble = Double.parseDouble(versionArray[0] + "." + versionArray[1] + versionArray[2]);
        return versionDouble;
    }

    /**
     * Merges multiple arrays into one.
     *
     * @param arrays String arrays that need to be combined
     * @return One array with contents of the multiple
     */
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
}
