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

    private final DateHolderFormatter yearFormatter;
    private final DateHolderFormatter dayFormatter;
    private final DateHolderFormatter secondFormatter;
    private final DateHolderFormatter clockFormatter;
    private final DateHolderFormatter iso8601NoClockFormatter;

    private final YearFormatter yearLongFormatter;
    private final DayFormatter dayLongFormatter;
    private final SecondFormatter secondLongFormatter;
    private final ClockFormatter clockLongFormatter;
    private final ISO8601NoClockFormatter iso8601NoClockLongFormatter;

    private final TimeAmountFormatter timeAmountFormatter;

    private final DecimalFormatter decimalFormatter;
    private final PercentageFormatter percentageFormatter;

    @Inject
    public Formatters(PlanConfig config) {
        yearLongFormatter = new YearFormatter(config);
        dayLongFormatter = new DayFormatter(config);
        clockLongFormatter = new ClockFormatter(config);
        secondLongFormatter = new SecondFormatter(config);
        iso8601NoClockLongFormatter = new ISO8601NoClockFormatter(config);

        yearFormatter = new DateHolderFormatter(yearLongFormatter);
        dayFormatter = new DateHolderFormatter(dayLongFormatter);
        secondFormatter = new DateHolderFormatter(secondLongFormatter);
        clockFormatter = new DateHolderFormatter(clockLongFormatter);
        iso8601NoClockFormatter = new DateHolderFormatter(iso8601NoClockLongFormatter);

        timeAmountFormatter = new TimeAmountFormatter(config);

        decimalFormatter = new DecimalFormatter(config);
        percentageFormatter = new PercentageFormatter(decimalFormatter);

    }

    public Formatter<DateHolder> year() {
        return this.yearFormatter;
    }

    public Formatter<Long> yearLong() {
        return yearLongFormatter;
    }

    public Formatter<DateHolder> day() {
        return dayFormatter;
    }

    public Formatter<Long> dayLong() {
        return dayLongFormatter;
    }

    public Formatter<DateHolder> second() {
        return secondFormatter;
    }

    public Formatter<Long> secondLong() {
        return secondLongFormatter;
    }

    public Formatter<DateHolder> clock() {
        return clockFormatter;
    }

    public Formatter<Long> clockLong() {
        return clockLongFormatter;
    }

    public Formatter<DateHolder> iso8601NoClock() {
        return iso8601NoClockFormatter;
    }

    public Formatter<Long> iso8601NoClockLong() {
        return iso8601NoClockLongFormatter;
    }

    public Formatter<Long> timeAmount() {
        return timeAmountFormatter;
    }

    public Formatter<Double> percentage() {
        return percentageFormatter;
    }

    public Formatter<Double> decimals() {
        return decimalFormatter;
    }
}