package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

/**
 * Formatter for timestamp which includes seconds as the smallest entry.
 *
 * @author Rsl1122
 */
public class SecondFormatter extends DateFormatter {

    public SecondFormatter(PlanConfig config) {
        super(config);
    }

    @Override
    public String apply(Long date) {
        return date > 0 ? format(date) : "-";
    }

    private String format(Long date) {
        String format = config.getString(Settings.FORMAT_DATE_FULL);

        if (config.isTrue(Settings.FORMAT_DATE_RECENT_DAYS)) {
            format = replaceRecentDays(date, format);
        }

        return format(date, format);
    }
}