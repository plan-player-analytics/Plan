package com.djrapitops.plan.data.store.mutators.formatting;

import com.djrapitops.plan.system.settings.Settings;
import org.apache.commons.lang3.StringUtils;

/**
 * Formatter for time amount in milliseconds.
 *
 * @author Rsl1122
 */
public class TimeAmountFormatter implements Formatter<Long> {

    // Placeholders for the config settings
    private final String zeroPH = "%zero%";
    private final String secondsPH = "%seconds%";
    private final String minutesPH = "%minutes%";
    private final String hoursPH = "%hours%";
    private final String daysPH = "%days%";
    private final String monthsPH = "%months%";
    private final String yearsPH = "%years%";

    @Override
    public String apply(Long ms) {
        if (ms <= 0) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        long x = ms / 1000;
        long seconds = x % 60;
        x /= 60;
        long minutes = x % 60;
        x /= 60;
        long hours = x % 24;
        x /= 24;
        long days = x % 365;
        long months = (days - days % 30) / 30;
        days -= months * 30;
        x /= 365;
        long years = x;

        appendYears(builder, years);
        appendMonths(builder, months);
        appendDays(builder, days);

        String hourFormat = Settings.FORMAT_HOURS.toString();
        String minuteFormat = Settings.FORMAT_MINUTES.toString();
        String secondFormat = Settings.FORMAT_SECONDS.toString();

        appendHours(builder, hours, hourFormat);
        appendMinutes(builder, minutes, hours, hourFormat, minuteFormat);
        appendSeconds(builder, seconds, minutes, hours, hourFormat, minuteFormat, secondFormat);

        String formattedTime = StringUtils.remove(builder.toString(), zeroPH);
        if (formattedTime.isEmpty()) {
            return Settings.FORMAT_ZERO_SECONDS.toString();
        }
        return formattedTime;
    }

    private void appendSeconds(StringBuilder builder, long seconds, long minutes, long hours, String fHours, String fMinutes, String fSeconds) {
        if (seconds != 0) {
            String s = fSeconds.replace(secondsPH, String.valueOf(seconds));
            if (minutes == 0 && s.contains(minutesPH)) {
                if (hours == 0 && fMinutes.contains(hoursPH)) {
                    builder.append(fHours.replace(zeroPH, "0").replace(hoursPH, "0"));
                }
                builder.append(fMinutes.replace(hoursPH, "").replace(zeroPH, "0").replace(minutesPH, "0"));
            }
            s = s.replace(minutesPH, "");
            if (s.contains(zeroPH) && String.valueOf(seconds).length() == 1) {
                builder.append('0');
            }
            builder.append(s);
        }
    }

    private void appendMinutes(StringBuilder builder, long minutes, long hours, String fHours, String fMinutes) {
        if (minutes != 0) {
            String m = fMinutes.replace(minutesPH, String.valueOf(minutes));
            if (hours == 0 && m.contains(hoursPH)) {
                builder.append(fHours.replace(zeroPH, "0").replace(hoursPH, "0"));
                m = m.replace(hoursPH, "");
            }
            m = m.replace(hoursPH, "");
            if (m.contains(zeroPH) && String.valueOf(minutes).length() == 1) {
                builder.append('0');
            }
            builder.append(m);
        }
    }

    private void appendHours(StringBuilder builder, long hours, String fHours) {
        if (hours != 0) {
            String h = fHours.replace(hoursPH, String.valueOf(hours));
            if (h.contains(zeroPH) && String.valueOf(hours).length() == 1) {
                builder.append('0');
            }
            builder.append(h);
        }
    }

    private void appendDays(StringBuilder builder, long days) {
        String singular = Settings.FORMAT_DAY.toString();
        String plural = Settings.FORMAT_DAYS.toString();
        appendValue(builder, days, singular, plural, daysPH);
    }

    private void appendMonths(StringBuilder builder, long months) {
        String singular = Settings.FORMAT_MONTH.toString();
        String plural = Settings.FORMAT_MONTHS.toString();

        appendValue(builder, months, singular, plural, monthsPH);
    }

    private void appendYears(StringBuilder builder, long years) {
        String singular = Settings.FORMAT_YEAR.toString();
        String plural = Settings.FORMAT_YEARS.toString();

        appendValue(builder, years, singular, plural, yearsPH);
    }

    private void appendValue(StringBuilder builder, long amount, String singular, String plural, String replace) {
        if (amount != 0) {
            if (amount == 1) {
                builder.append(singular);
            } else {
                builder.append(plural.replace(replace, String.valueOf(amount)));
            }
        }
    }
}