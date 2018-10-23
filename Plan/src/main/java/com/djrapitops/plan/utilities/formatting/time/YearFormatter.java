package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

/**
 * Formatter for a timestamp which includes year, but not seconds.
 *
 * @author Rsl1122
 */
public class YearFormatter extends DateFormatter {

    public YearFormatter(PlanConfig config, Locale locale) {
        super(config, locale);
    }

    @Override
    public String apply(Long date) {
        return date > 0 ? format(date) : "-";
    }

    private String format(Long date) {
        String format = config.getString(Settings.FORMAT_DATE_NO_SECONDS);

        if (config.isTrue(Settings.FORMAT_DATE_RECENT_DAYS)) {
            format = replaceRecentDays(date, format);
        }
        return format(date, format);
    }
}