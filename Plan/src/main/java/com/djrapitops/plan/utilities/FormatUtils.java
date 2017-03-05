package main.java.com.djrapitops.plan.utilities;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import main.java.com.djrapitops.plan.ui.Html;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class FormatUtils {

    /**
     * Formats a String of long in ms to readable format.
     *
     * @param string String of a Long in ms
     * @return Readable format
     * @throws NumberFormatException if String is not long.
     */
    public static String formatTimeAmount(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        return turnMsLongToString(ms);
    }

    /**
     * Formats the difference between the two dates into readable format.
     *
     * @param before
     * @param now
     * @return Readable format
     * @throws NumberFormatException
     */
    public static String formatTimeAmountSinceDate(Date before, Date now) throws NumberFormatException {
        long ms = Math.abs((now.toInstant().getEpochSecond() * 1000) - (before.toInstant().getEpochSecond() * 1000));
        return turnMsLongToString(ms);
    }

    /**
     * Creates a new Date with Epoch second and returns Date and Time String.
     *
     * @param string
     * @return Readable TimeStamp
     * @throws NumberFormatException String is not Long
     */
    public static String formatTimeStamp(String string) throws NumberFormatException {
        long ms = Long.parseLong(string);
        Date sfd = new Date(ms);
        return ("" + sfd).substring(4, 19);
    }

    /**
     * Formats the difference between the two dates, where first is in String of
     * Long format, into readable format.
     *
     * @param string Long in ms, date
     * @param now
     * @return
     * @throws NumberFormatException
     */
    public static String formatTimeAmountSinceString(String string, Date now) throws NumberFormatException {
        long ms = Math.abs((now.toInstant().getEpochSecond() * 1000) - Long.parseLong(string));
        return turnMsLongToString(ms);
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
        int main = Integer.parseInt(versionArray[0]) * 100;
        int major = Integer.parseInt(versionArray[1]) * 10;
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

    static String swapColorsToSpan(String string) {
        Html[] replacer = new Html[]{Html.COLOR_0, Html.COLOR_1, Html.COLOR_2, Html.COLOR_3,
            Html.COLOR_4, Html.COLOR_5, Html.COLOR_6, Html.COLOR_7, Html.COLOR_8, Html.COLOR_9,
            Html.COLOR_a, Html.COLOR_b, Html.COLOR_c, Html.COLOR_d, Html.COLOR_e, Html.COLOR_f};

        for (Html html : replacer) {
            string = string.replaceAll("§" + html.name().charAt(6), html.parse());
        }
        int spans = string.split("<span").length - 1;
        for (int i = 0; i < spans; i++) {
            string = Html.SPAN.parse(string);
        }
        return string.replaceAll("§r", "");
    }

    /**
     *
     * @param d
     * @return
     */
    public static String cutDecimals(double d) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(d);
    }
}
