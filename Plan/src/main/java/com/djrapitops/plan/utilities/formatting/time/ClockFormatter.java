package com.djrapitops.plan.utilities.formatting.time;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class ClockFormatter extends DateFormatter {

    public ClockFormatter(PlanConfig config) {
        super(config);
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