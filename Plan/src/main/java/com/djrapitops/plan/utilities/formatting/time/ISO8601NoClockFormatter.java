package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.settings.config.PlanConfig;

/**
 * Formatter for a timestamp in ISO-8601 format without the clock.
 *
 * @author Rsl1122
 */
public class ISO8601NoClockFormatter extends DateFormatter {

    public ISO8601NoClockFormatter(PlanConfig config) {
        super(config);
    }

    @Override
    public String apply(Long date) {
        return date > 0 ? format(date) : "-";
    }

    private String format(Long date) {
        String format = "yyyy-MM-dd";

        return format(date, format);
    }
}