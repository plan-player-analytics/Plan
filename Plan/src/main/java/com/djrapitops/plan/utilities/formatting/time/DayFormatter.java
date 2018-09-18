package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

/**
 * Formatter for a timestamp which includes days as the smallest entry.
 *
 * @author Rsl1122
 */
public class DayFormatter extends DateFormatter {

    public DayFormatter(PlanConfig config) {
        super(config);
    }

    @Override
    public String apply(Long date) {
        return date > 0 ? format(date) : "-";
    }

    private String format(Long date) {
        String format = "MMMMM d";

        if (config.isTrue(Settings.FORMAT_DATE_RECENT_DAYS)) {
            format = replaceRecentDays(date, format, "MMMMM");
        }

        return format(date, format);
    }
}