package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.formatting.time.*;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Factory for new instances of different {@link Formatter}s.
 *
 * @author Rsl1122
 */
@Singleton
public class Formatters {

    private PlanConfig config;

    @Inject
    public Formatters(PlanConfig config) {
        this.config = config;
    }

    public Formatter<DateHolder> year() {
        return new DateHolderFormatter(yearLong());
    }

    public Formatter<Long> yearLong() {
        return new YearFormatter(config);
    }

    public Formatter<DateHolder> day() {
        return new DateHolderFormatter(dayLong());
    }

    public Formatter<Long> dayLong() {
        return new DayFormatter(config);
    }

    public Formatter<DateHolder> second() {
        return new DateHolderFormatter(secondLong());
    }

    public Formatter<Long> secondLong() {
        return new SecondFormatter(config);
    }

    public Formatter<DateHolder> clock() {
        return new DateHolderFormatter(clockLong());
    }

    public Formatter<Long> clockLong() {
        return new ClockFormatter(config);
    }

    public Formatter<DateHolder> iso8601NoClock() {
        return new DateHolderFormatter(iso8601NoClockLong());
    }

    public Formatter<Long> iso8601NoClockLong() {
        return new ISO8601NoClockFormatter(config);
    }

    public Formatter<Long> timeAmount() {
        return new TimeAmountFormatter(config);
    }

    public Formatter<Double> percentage() {
        return new PercentageFormatter(decimals());
    }

    public Formatter<Double> decimals() {
        return new DecimalFormatter(config);
    }
}