package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;


/**
 * Formatter for a timestamp that only includes a clock.
 *
 * @author Rsl1122
 */
public class ClockFormatter extends DateFormatter {

    public ClockFormatter(PlanConfig config, Locale locale) {
        super(config, locale);
    }

    @Override
    public String apply(Long date) {
        return date > 0 ? format(date) : "-";
    }

    private String format(Long date) {
        String format = config.getString(Settings.FORMAT_DATE_CLOCK);

        return format(date, format);
    }
}