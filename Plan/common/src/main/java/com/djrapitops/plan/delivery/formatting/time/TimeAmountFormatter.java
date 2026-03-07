/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.formatting.time;

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.FormatSettings;
import org.apache.commons.lang3.Strings;

/**
 * Formatter for time amount in milliseconds.
 *
 * @author AuroraLS3
 */
public class TimeAmountFormatter implements Formatter<Long> {

    // Placeholders for the config settings
    private static final String ZERO_PH = "%zero%";
    private static final String SECONDS_PH = "%seconds%";
    private static final String MINUTES_PH = "%minutes%";
    private static final String HOURS_PH = "%hours%";
    private static final String DAYS_PH = "%days%";
    private static final String MONTHS_PH = "%months%";
    private static final String YEARS_PH = "%years%";

    private final PlanConfig config;

    public TimeAmountFormatter(PlanConfig config) {
        this.config = config;
    }

    @Override
    public String apply(Long ms) {
        if (ms == null || ms < 0) {
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

        String hourFormat = config.get(FormatSettings.HOURS);
        String minuteFormat = config.get(FormatSettings.MINUTES);
        String secondFormat = config.get(FormatSettings.SECONDS);

        appendHours(builder, hours, hourFormat);
        appendMinutes(builder, minutes, hours, hourFormat, minuteFormat);
        appendSeconds(builder, seconds, minutes, hours, hourFormat, minuteFormat, secondFormat);

        String formattedTime = Strings.CS.remove(builder.toString(), ZERO_PH);
        if (formattedTime.isEmpty()) {
            return config.get(FormatSettings.ZERO_SECONDS);
        }
        return formattedTime;
    }

    private void appendHours(StringBuilder builder, long hours, String fHours) {
        if (hours != 0) {
            String h = fHours.replace(HOURS_PH, String.valueOf(hours));
            if (h.contains(ZERO_PH) && String.valueOf(hours).length() == 1) {
                builder.append('0');
            }
            builder.append(h);
        }
    }

    private void appendMinutes(StringBuilder builder, long minutes, long hours, String fHours, String fMinutes) {
        if (minutes != 0) {
            String m = fMinutes.replace(MINUTES_PH, String.valueOf(minutes));
            if (hours == 0 && m.contains(HOURS_PH)) {
                builder.append(fHours.replace(ZERO_PH, "0").replace(HOURS_PH, "0"));
                m = m.replace(HOURS_PH, "");
            }
            m = m.replace(HOURS_PH, "");
            if (m.contains(ZERO_PH) && String.valueOf(minutes).length() == 1) {
                builder.append('0');
            }
            builder.append(m);
        }
    }

    private void appendSeconds(StringBuilder builder, long seconds, long minutes, long hours, String fHours, String fMinutes, String fSeconds) {
        if (seconds != 0 || fSeconds.contains(ZERO_PH)) {
            String s = fSeconds.replace(SECONDS_PH, String.valueOf(seconds));
            if (minutes == 0 && s.contains(MINUTES_PH)) {
                if (hours == 0 && fMinutes.contains(HOURS_PH)) {
                    builder.append(fHours.replace(ZERO_PH, "0").replace(HOURS_PH, "0"));
                }
                builder.append(fMinutes.replace(HOURS_PH, "").replace(ZERO_PH, "0").replace(MINUTES_PH, "0"));
            }
            s = s.replace(MINUTES_PH, "");
            if (s.contains(ZERO_PH) && String.valueOf(seconds).length() == 1) {
                builder.append('0');
            }
            builder.append(s);
        }
    }

    private void appendDays(StringBuilder builder, long days) {
        String singular = config.get(FormatSettings.DAY);
        String plural = config.get(FormatSettings.DAYS);
        appendValue(builder, days, singular, plural, DAYS_PH);
    }

    private void appendMonths(StringBuilder builder, long months) {
        String singular = config.get(FormatSettings.MONTH);
        String plural = config.get(FormatSettings.MONTHS);

        appendValue(builder, months, singular, plural, MONTHS_PH);
    }

    private void appendYears(StringBuilder builder, long years) {
        String singular = config.get(FormatSettings.YEAR);
        String plural = config.get(FormatSettings.YEARS);

        appendValue(builder, years, singular, plural, YEARS_PH);
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