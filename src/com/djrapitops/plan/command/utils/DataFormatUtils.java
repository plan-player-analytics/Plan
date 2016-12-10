
package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

public class DataFormatUtils {
    
    public static HashMap<String, String> removeExtraDataPoints(HashMap<String, String> data) throws NumberFormatException {
        List<String> remove = new ArrayList<>();
        Plan plugin = getPlugin(Plan.class);
        for (String key : data.keySet()) {
            try {
                // Process OnTime empty data (returns -1 if empty)
                if (key.subSequence(0, 3).equals("ONT")) {
                    if ((data.get(key)).equals("-1") || (data.get(key)).equals("-1.0")) {
                        remove.add(key);
                    }
                }
                // Process failed PlaceholderAPI requests (%string%)
                if (key.subSequence(0, 3).equals("PHA")) {
                    if ((data.get(key)).contains("%")) {
                        remove.add(key);
                    }
                }
            } catch (Exception e) {
                plugin.logToFile("FORMAT-Remove\n" + e + "\n" + key);
            }
        }
        // Remove faulty data to prevent TOW-LAST LOGIN from being removed with empty data
        for (String removedKey : remove) {
            data.remove(removedKey);
        }
        remove.clear();
        // Process Towny data (Empty returns Java Epoch date 1970 for REGISTERED)
        if (data.get("TOW-REGISTERED") != null) {
            if (data.get("TOW-REGISTERED").contains("1970")) {
                remove.add("TOW-REGISTERED");
                remove.add("TOW-ONLINE");
                remove.add("TOW-LAST LOGIN");
                remove.add("TOW-OWNER OF");
                if (data.get("TOW-FRIENDS") != null) {
                    remove.add("TOW-FRIENDS");
                    remove.add("TOW-PLOT PERMS");
                    remove.add("TOW-PLOT OPTIONS");
                }
            }
            // If both OnTime and Towny data found, OnTime priority.
            if (data.get("ONT-LAST LOGIN") != null) {
                remove.add("TOW-LAST LOGIN");
            }
        }
        // Remove faulty Towny data
        for (String removedKey : remove) {
            data.remove(removedKey);
        }
        // Remove faulty Essentials SINCE data, reload turns data to 0
        String[] keysRemoveIfZero = {"ESS-ONLINE SINCE", "ESS-OFFLINE SINCE"};
        for (String key : keysRemoveIfZero) {
            if (data.get(key) != null) {
                if (data.get(key).equals("0")) {
                    data.remove(key);
                }
            }
        }
        // Remove OnTime Total Votes if SuperbVote is present
        if (data.get("SVO-VOTES") != null) {
            if (data.get("ONT-TOTAL VOTES") != null) {
                data.remove("ONT-TOTAL VOTES");
            }
        }
        // Format TimeStamps
        String[] keysTimestamp = {"ONT-LAST LOGIN"};
        for (String key : keysTimestamp) {
            if (data.get(key) != null) {
                try {
                    String formatted = formatTimeStamp(data.get(key));
                    data.replace(key, formatted);
                } catch (NumberFormatException e) {

                    plugin.logToFile("FORMAT-TimeStamp\nError Parsing Last Login.\n" + e + "\n" + data.get(key));

                    data.remove(key);
                }
            }
        }
        // Format Milliseconds to readable format
        String[] keysTimeAmount = {"ONT-TOTAL PLAY", "ESS-ONLINE SINCE", "ESS-OFFLINE SINCE"};
        for (String key : keysTimeAmount) {
            if (data.get(key) != null) {
                try {
                    String formatted;
                    if (key.equals("ONT-TOTAL PLAY")) {
                        formatted = formatTimeAmount(data.get(key));
                    } else {
                        formatted = formatTimeAmountSinceString(data.get(key), new Date());
                    }
                    if (formatted != null) {
                        data.replace(key, formatted);
                    }
                } catch (NumberFormatException e) {
                    plugin.logToFile("FORMAT-Since\nError Parsing number.\n" + e + "\n" + data.get(key));
                    data.remove(key);
                }
            }
        }
        return data;
    }
    // Creates a new Date with Epoch second and returns Date and Time String
    public static String formatTimeStamp(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        Date sfd = new Date(ms);
        return ("" + sfd).substring(4, 19);
    }
    
    // Formats Time Since (0 -> string)
    public static String formatTimeAmount(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        return turnMsLongToString(ms);
    }
    
    // Formats Time Difference String before -> Date now
    public static String formatTimeAmountSinceString(String string, Date date) throws NumberFormatException {
        long ms = (date.toInstant().getEpochSecond() * 1000) - Long.parseLong(string);
        return turnMsLongToString(ms);
    }
    
    // Formats Time Difference Date before -> Date now
    public static String formatTimeAmountSinceDate(Date before, Date now) throws NumberFormatException {
        long ms = (now.toInstant().getEpochSecond() * 1000) - (before.toInstant().getEpochSecond() * 1000);
        return turnMsLongToString(ms);
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
        return returnValue;
    }

    // Analysis data Formatting, will be updated after more analysis is added
    public static HashMap<String, String> formatAnalyzed(HashMap<String, String> analyzedData) {
        return removeExtraDataPoints(analyzedData);
    }

    // Removes letters from a string leaving only numbers and dots.
    public static String removeLetters(String dataPoint) {
        String numbers = "0123456789.";
        List<Character> numList = new ArrayList<>();
        char[] numberArray = numbers.toCharArray();
        for (char c : numberArray) {
            numList.add(c);
        }
        String returnString = "";
        for (int i = 0; i < dataPoint.length(); i++) {
            if (numList.contains(dataPoint.charAt(i))) {
                returnString += dataPoint.charAt(i);
            }
        }
        return returnString;
    }
    
    // Sorts HashMap into Sorted List of Arrays
    public static List<String[]> turnDataHashMapToSortedListOfArrays(HashMap<String, String> data) {
        List<String[]> dataList = new ArrayList<>();
        for (String key : data.keySet()) {
            dataList.add(new String[]{key, data.get(key)});
        }
        Collections.sort(dataList, new Comparator<String[]>() {
            public int compare(String[] strings, String[] otherStrings) {
                return strings[0].compareTo(otherStrings[0]);
            }
        });
        return dataList;
    }
}
