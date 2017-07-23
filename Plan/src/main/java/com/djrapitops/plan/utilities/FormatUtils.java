package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.FormattingUtils;
import java.text.DecimalFormat;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Location;

/**
 *
 * @author Rsl1122
 */
public class FormatUtils {

    /**
     *
     * @param ms
     * @return
     */
    public static String formatTimeAmount(long ms) {
        return formatMilliseconds(ms);
    }

    /**
     *
     * @param before
     * @param after
     * @return
     */
    public static String formatTimeAmountDifference(long before, long after) {
        return formatMilliseconds(Math.abs(after - before));
    }

    /**
     *
     * @param epochMs
     * @return
     */
    public static String formatTimeStamp(long epochMs) {
        return FormattingUtils.formatTimeStamp(epochMs);
    }

    /**
     *
     * @param epochMs
     * @return
     */
    public static String formatTimeStampSecond(long epochMs) {
        return FormattingUtils.formatTimeStampSecond(epochMs);
    }

    /**
     *
     * @param epochMs
     * @return
     */
    public static String formatTimeStampYear(long epochMs) {
        return FormattingUtils.formatTimeStampYear(epochMs);
    }

    /**
     * Removes letters from a string leaving only numbers and dots.
     *
     * @param dataPoint
     * @return
     */
    public static String removeLetters(String dataPoint) {
        return Format.create(dataPoint)
                .removeLetters()
                .removeWhitespace()
                .removeSymbolsButDot()
                .toString();
    }

    /**
     *
     * @param dataPoint
     * @return
     */
    public static String removeNumbers(String dataPoint) {
        return Format.create(dataPoint)
                .removeNumbers()
                .removeWhitespace()
                .removeDot()
                .toString();
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
        long days = x % 365;
        x /= 365;
        long years = x;
        if (years != 0) {
            if (years == 1) {
                builder.append(Settings.FORMAT_YEAR.toString());
            } else {
                builder.append(Settings.FORMAT_YEARS.toString().replace("%years%", "" + years));
            }
        }
        if (days != 0) {
            if (days == 1) {
                builder.append(Settings.FORMAT_DAY.toString());
            } else {
                builder.append(Settings.FORMAT_DAYS.toString().replace("%days%", "" + days));
            }
        }
        if (hours != 0) {
            String h = Settings.FORMAT_HOURS.toString().replace("%hours%", "" + hours);
            if (h.contains("%zero%") && (hours + "").length() == 1) {
                builder.append('0');
            }
            builder.append(h);
        }
        if (minutes != 0) {
            String m = Settings.FORMAT_MINUTES.toString().replace("%minutes%", "" + minutes);
            if (m.contains("%zero%") && (minutes + "").length() == 1) {
                builder.append('0');
            }
            builder.append(m);
        }
        if (seconds != 0) {
            String s = Settings.FORMAT_SECONDS.toString().replace("%seconds%", "" + seconds);
            if (s.contains("%zero%") && (seconds + "").length() == 1) {
                builder.append('0');
            }
            builder.append(s);
        }
        String formattedTime = builder.toString().replace("%zero%", "");
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
        return FormattingUtils.parseVersionNumber(versionString);
    }

    /**
     * Merges multiple arrays into one.
     *
     * @param arrays String arrays that need to be combined
     * @return One array with contents of the multiple
     */
    public static String[] mergeArrays(String[]... arrays) {
        return FormattingUtils.mergeArrays(arrays);
    }

    /**
     * Formats a Minecraft Location into readable format.
     *
     * @param loc Location to format
     * @return Readable location format.
     */
    public static String formatLocation(Location loc) {
        return FormattingUtils.formatLocation(loc);
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
