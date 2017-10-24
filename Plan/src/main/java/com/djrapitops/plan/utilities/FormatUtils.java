package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plugin.utilities.Format;
import com.djrapitops.plugin.utilities.FormattingUtils;
import main.java.com.djrapitops.plan.Settings;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;

import java.text.DecimalFormat;

/**
 * @author Rsl1122
 */
public class FormatUtils {

    private static final DecimalFormat df = new DecimalFormat(Settings.FORMAT_DECIMALS.toString());

    /**
     * Constructor used to hide the public constructor
     */
    private FormatUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Format time amount according to the config settings.
     *
     * @param ms Milliseconds.
     * @return String formatted with the config settings.
     */
    public static String formatTimeAmount(long ms) {
        return formatMilliseconds(ms);
    }

    /**
     * Format the time difference between an end point and a start point.
     *
     * @param before Epoch ms.
     * @param after  Epoch ms.
     * @return String formatted with the config settings.
     */
    public static String formatTimeAmountDifference(long before, long after) {
        return formatMilliseconds(Math.abs(after - before));
    }

    public static String formatTimeStamp(long epochMs) {
        return FormattingUtils.formatTimeStamp(epochMs);
    }

    public static String formatTimeStampSecond(long epochMs) {
        return FormattingUtils.formatTimeStampSecond(epochMs);
    }

    public static String formatTimeStampYear(long epochMs) {
        return FormattingUtils.formatTimeStampYear(epochMs);
    }

    /**
     * Removes letters from a string leaving only numbers and dots.
     *
     * @param dataPoint String to remove stuff from.
     * @return String with the stuff removed.
     */
    public static String removeLetters(String dataPoint) {
        return Format.create(dataPoint)
                .removeLetters()
                .removeWhitespace()
                .removeSymbolsButDot()
                .toString();
    }

    /**
     * Removes numbers from a string leaving only letters.
     *
     * @param dataPoint String to remove stuff from.
     * @return String with the stuff removed.
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
                builder.append(Settings.FORMAT_YEARS.toString().replace("%years%", String.valueOf(years)));
            }
        }
        if (days != 0) {
            if (days == 1) {
                builder.append(Settings.FORMAT_DAY.toString());
            } else {
                builder.append(Settings.FORMAT_DAYS.toString().replace("%days%", String.valueOf(days)));
            }
        }
        if (hours != 0) {
            String h = Settings.FORMAT_HOURS.toString().replace("%hours%", String.valueOf(hours));
            if (h.contains("%zero%") && String.valueOf(hours).length() == 1) {
                builder.append('0');
            }
            builder.append(h);
        }
        if (minutes != 0) {
            String m = Settings.FORMAT_MINUTES.toString().replace("%minutes%", String.valueOf(minutes));
            if (hours == 0 && m.contains("%hours%")) {
                m = m.replace("%hours%", Settings.FORMAT_MINUTES.toString().replace("%zero%", "0") + "0");
            } else {
                m = m.replace("%hours", "");
            }
            if (m.contains("%zero%") && String.valueOf(minutes).length() == 1) {
                builder.append('0');
            }
            builder.append(m);
        }
        if (seconds != 0) {
            String s = Settings.FORMAT_SECONDS.toString().replace("%seconds%", String.valueOf(seconds));
            if (minutes == 0 && s.contains("%minutes%")) {
                s = s.replace("%minutes%", Settings.FORMAT_MINUTES.toString().replace("%hours", "").replace("%zero%", "0") + 0);
            } else {
                s = s.replace("%minutes%", "");
            }
            if (s.contains("%zero%") && String.valueOf(seconds).length() == 1) {
                builder.append('0');
            }
            builder.append(s);
        }
        String formattedTime = StringUtils.remove(builder.toString(), "%zero%");
        if (formattedTime.isEmpty()) {
            return Settings.FORMAT_ZERO_SECONDS.toString();
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
    public static int parseVersionNumber(String versionString) {
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
     * Remove extra decimals from the end of the double.
     *
     * @param d Double.
     * @return String format of the double.
     */
    public static String cutDecimals(double d) {
        return df.format(d);
    }
}
