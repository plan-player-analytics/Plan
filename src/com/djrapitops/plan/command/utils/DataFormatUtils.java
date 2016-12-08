
package com.djrapitops.plan.command.utils;

import com.djrapitops.plan.Plan;
import java.util.ArrayList;
import java.util.Arrays;
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
                if (key.subSequence(0, 3).equals("ONT")) {
                    if ((data.get(key)).equals("-1") || (data.get(key)).equals("-1.0")) {
                        remove.add(key);
                    }
                }
                if (key.subSequence(0, 3).equals("PHA")) {
                    if ((data.get(key)).contains("%")) {
                        remove.add(key);
                    }
                }
            } catch (Exception e) {
                plugin.logToFile("FORMAT-Remove\n" + e + "\n" + key);
            }
        }
        for (String removedKey : remove) {
            data.remove(removedKey);
        }
        remove.clear();
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

            if (data.get("ONT-LAST LOGIN") != null) {
                remove.add("TOW-LAST LOGIN");
            }
        }
        for (String removedKey : remove) {
            data.remove(removedKey);
        }
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
        String[] keysRemoveIfZero = {"ESS-ONLINE SINCE", "ESS-OFFLINE SINCE"};
        for (String key : keysRemoveIfZero) {
            if (data.get(key) != null) {
                if (data.get(key).equals("0")) {
                    data.remove(key);
                }
            }
        }

        String[] keysTimeAmount = {"ONT-TOTAL PLAY", "ESS-ONLINE SINCE", "ESS-OFFLINE SINCE"};
        for (String key : keysTimeAmount) {
            if (data.get(key) != null) {
                try {
                    String formatted;
                    if (key.equals("ONT-TOTAL PLAY")) {
                        formatted = formatTimeAmount(data.get(key));
                    } else {
                        formatted = formatTimeAmount(data.get(key), new Date());
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
        if (data.get("SVO-VOTES") != null) {
            if (data.get("ONT-TOTAL VOTES") != null) {
                data.remove("ONT-TOTAL VOTES");
            }
        }
        return data;
    }
    
    public static String formatTimeStamp(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        Date sfd = new Date(ms);
        return ("" + sfd).substring(4, 19);
    }
    
    public static String formatTimeAmount(String string) throws NumberFormatException {
        String returnValue = "";
        long ms = Long.parseLong(string);
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
    
    public static String formatTimeAmount(String string, Date date) throws NumberFormatException {
        String returnValue = "";
        long ms = (date.toInstant().getEpochSecond() * 1000) - Long.parseLong(string);
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
    public static String formatTimeAmount(Date before, Date now) throws NumberFormatException {
        String returnValue = "";
        long ms = (now.toInstant().getEpochSecond() * 1000) - (before.toInstant().getEpochSecond() * 1000);
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

    public static HashMap<String, String> formatAnalyzed(HashMap<String, String> analyzedData) {
        return removeExtraDataPoints(analyzedData);
    }

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
}
