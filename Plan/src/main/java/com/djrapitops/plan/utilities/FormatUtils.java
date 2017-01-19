package com.djrapitops.plan.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class FormatUtils {

    // Formats Time Since (0 -> string)
    public static String formatTimeAmount(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        return turnMsLongToString(ms);
    }

    // Formats Time Difference Date before -> Date now
    public static String formatTimeAmountSinceDate(Date before, Date now) throws NumberFormatException {
        long ms = Math.abs((now.toInstant().getEpochSecond() * 1000) - (before.toInstant().getEpochSecond() * 1000));
        return turnMsLongToString(ms);
    }

    // Creates a new Date with Epoch second and returns Date and Time String
    public static String formatTimeStamp(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        Date sfd = new Date(ms);
        return ("" + sfd).substring(4, 19);
    }

    // Formats Time Difference String before -> Date now
    public static String formatTimeAmountSinceString(String string, Date now) throws NumberFormatException {
        long ms = Math.abs((now.toInstant().getEpochSecond() * 1000) - Long.parseLong(string));
        return turnMsLongToString(ms);
    }

    // Removes letters from a string leaving only numbers and dots.
    public static String removeLetters(String dataPoint) {        
        return dataPoint.replaceAll("[^\\d.]", "");
    }

    // Formats long in milliseconds into d:h:m:s string
    private static String turnMsLongToString(long ms) {
        String returnValue = "";
        long x = ms / 1000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        x /= 24;
        long days = x;
        if (days != 0) {
            returnValue += days + "d ";
        }
        if (hours != 0) {
            returnValue += hours + "h ";
        }
        if (minutes != 0) {
            returnValue += minutes + "m ";
        }
        if (seconds != 0) {
            returnValue += seconds + "s";
        }
        if (returnValue.isEmpty()) {
            returnValue += "0s";
        }
        return returnValue;
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

    public static String formatLocation(Location loc) {
        return "x " + loc.getBlockX() + " z " + loc.getBlockZ() + " in " + loc.getWorld();
    }

}
