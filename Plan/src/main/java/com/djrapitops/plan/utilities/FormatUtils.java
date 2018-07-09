package com.djrapitops.plan.utilities;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Rsl1122
 */
public class FormatUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private FormatUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String formatTimeStampISO8601NoClock(long epochMs) {
        String format = "yyyy-MM-dd";

        return format(epochMs, format);
    }

    public static String formatTimeStampDay(long epochMs) {
        String format = "MMMMM d";

        if (Settings.FORMAT_DATE_RECENT_DAYS.isTrue()) {
            format = replaceRecentDays(epochMs, format, "MMMMM");
        }

        return format(epochMs, format);
    }

    public static String formatTimeStampClock(long epochMs) {
        String format = Settings.FORMAT_DATE_CLOCK.toString();

        return format(epochMs, format);
    }

    private static String format(long epochMs, String format) {
        boolean useServerTime = Settings.USE_SERVER_TIME.isTrue();
        String locale = Settings.LOCALE.toString();
        Locale usedLocale = locale.equalsIgnoreCase("default") ? Locale.ENGLISH : Locale.forLanguageTag(locale);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, usedLocale);
        TimeZone timeZone = useServerTime ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT");
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(epochMs);
    }

    public static String formatTimeStampSecond(long epochMs) {
        String format = Settings.FORMAT_DATE_FULL.toString();

        if (Settings.FORMAT_DATE_RECENT_DAYS.isTrue()) {
            format = replaceRecentDays(epochMs, format);
        }

        return format(epochMs, format);
    }

    private static String replaceRecentDays(long epochMs, String format) {
        return replaceRecentDays(epochMs, format, Settings.FORMAT_DATE_RECENT_DAYS_PATTERN.toString());
    }

    private static String replaceRecentDays(long epochMs, String format, String pattern) {
        long now = System.currentTimeMillis();

        long fromStartOfDay = now % TimeAmount.DAY.ms();
        if (epochMs > now - fromStartOfDay) {
            format = format.replace(pattern, "'Today'");
        } else if (epochMs > now - TimeAmount.DAY.ms() - fromStartOfDay) {
            format = format.replace(pattern, "'Yesterday'");
        } else if (epochMs > now - TimeAmount.DAY.ms() * 5L) {
            format = format.replace(pattern, "EEEE");
        }
        return format;
    }

    public static String formatTimeStampYear(long epochMs) {
        String format = Settings.FORMAT_DATE_NO_SECONDS.toString();

        if (Settings.FORMAT_DATE_RECENT_DAYS.isTrue()) {
            format = replaceRecentDays(epochMs, format);
        }
        return format(epochMs, format);
    }

    /**
     * Merges multiple arrays into one.
     *
     * @param arrays String arrays that need to be combined
     * @return One array with contents of the multiple
     */
    public static String[] mergeArrays(String[]... arrays) {
        return com.djrapitops.plugin.utilities.FormatUtils.mergeArrays(arrays);
    }

    /**
     * Remove extra decimals from the end of the double.
     *
     * @param d Double.
     * @return String format of the double.
     */
    public static String cutDecimals(double d) {
        return new DecimalFormat(Settings.FORMAT_DECIMALS.toString()).format(d);
    }

    public static String formatIP(InetAddress address) {
        String ip = address.getHostAddress();
        if ("localhost".equals(ip)) {
            return ip;
        }
        if (address instanceof Inet6Address) {
            StringBuilder b = new StringBuilder();
            int i = 0;
            for (String part : ip.split(":")) {
                if (i >= 3) {
                    break;
                }

                b.append(part).append(':');

                i++;
            }

            return b.append("xx..").toString();
        } else {
            StringBuilder b = new StringBuilder();
            int i = 0;
            for (String part : ip.split("\\.")) {
                if (i >= 2) {
                    break;
                }

                b.append(part).append('.');

                i++;
            }

            return b.append("xx.xx").toString();
        }
    }

    /**
     * Gets lines for stack trace recursively.
     *
     * @param throwable Throwable element
     * @return lines of stack trace.
     */
    public static List<String> getStackTrace(Throwable throwable) {
        List<String> stackTrace = new ArrayList<>();
        stackTrace.add(throwable.toString());
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.add("    " + element.toString());
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            List<String> causeTrace = getStackTrace(cause);
            if (!causeTrace.isEmpty()) {
                causeTrace.set(0, "Caused by: " + causeTrace.get(0));
                stackTrace.addAll(causeTrace);
            }
        }

        return stackTrace;
    }
}
