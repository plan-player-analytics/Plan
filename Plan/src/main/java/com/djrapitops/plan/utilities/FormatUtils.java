package main.java.com.djrapitops.plan.utilities;

import java.text.DecimalFormat;
import java.util.Date;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class FormatUtils {

    public static String formatTimeAmount(long ms) {
        return formatMilliseconds(ms);
    }

    public static String formatTimeAmountDifference(long before, long after) {
        return formatMilliseconds(Math.abs(after - before));
    }

    public static String formatTimeStamp(long epochMs) {
        Date sfd = new Date(epochMs);
        return ("" + sfd).substring(4, 19);
    }

    /**
     * Removes letters from a string leaving only numbers and dots.
     *
     * @param dataPoint
     * @return
     */
    public static String removeLetters(String dataPoint) {
        return dataPoint.replaceAll("[^\\d.]", "");
    }

    /**
     *
     * @param dataPoint
     * @return
     */
    public static String removeNumbers(String dataPoint) {
        for (char c : removeLetters(dataPoint).toCharArray()) {
            dataPoint = dataPoint.replace(c + "", "");
        }
        dataPoint = dataPoint.replace(" ", "");
        return dataPoint;
    }

    // Formats long in milliseconds into d:h:m:s string
    private static String formatMilliseconds(long ms) {
        StringBuilder builder = new StringBuilder();
        long x = ms / 1000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        x /= 24;
        long days = x;
        if (days != 0) {
            builder.append(Settings.FORMAT_DAYS.toString().replace("%days%", ""+days));
        }
        if (hours != 0) {
            builder.append(Settings.FORMAT_HOURS.toString().replace("%hours%", ""+hours));
        }
        if (minutes != 0) {
            builder.append(Settings.FORMAT_MINUTES.toString().replace("%minutes%", ""+minutes));
        }
        if (seconds != 0) {
            builder.append(Settings.FORMAT_SECONDS.toString().replace("%seconds%", ""+seconds));
        }
        String formattedTime = builder.toString();
        if (formattedTime.isEmpty()) {
            return Settings.FORMAT_SECONDS.toString().replace("%seconds%", "0");
        }
        return formattedTime;
    }

    /**
     * Turns the version string into a integer
     *
     * @param versionString String - number format 1.1.1
     * @return parsed double - for example 1,11
     * @throws NumberFormatException When wrong format
     */
    public static int parseVersionNumber(String versionString) throws NumberFormatException {
        String[] versionArray = versionString.split("\\.");
        if (versionArray.length != 3) {
            throw new NumberFormatException("Wrong format used");
        }
        int main = Integer.parseInt(versionArray[0]) * 10000;
        int major = Integer.parseInt(versionArray[1]) * 100;
        int minor = Integer.parseInt(versionArray[2]);
        int versionNumber = main + major + minor;
        return versionNumber;
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

    /**
     * Formats a Minecraft Location into readable format.
     *
     * @param loc Location to format
     * @return Readable location format.
     */
    public static String formatLocation(Location loc) {
        return "x " + loc.getBlockX() + " z " + loc.getBlockZ() + " in " + loc.getWorld();
    }

    /**
     *
     * @param d
     * @return
     */
    public static String cutDecimals(double d) {
        DecimalFormat df = new DecimalFormat(Settings.FORMAT_DECIMALS.toString());
//        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(d);
    }
}
