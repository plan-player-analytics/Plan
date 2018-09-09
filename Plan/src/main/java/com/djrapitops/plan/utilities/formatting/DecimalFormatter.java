package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

import java.text.DecimalFormat;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public class DecimalFormatter implements Formatter<Double> {

    private final PlanConfig config;

    public DecimalFormatter(PlanConfig config) {
        this.config = config;
    }

    @Override
    public String apply(Double value) {
        return new DecimalFormat(config.getString(Settings.FORMAT_DECIMALS)).format(value);
    }
}