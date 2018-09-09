package com.djrapitops.plan.utilities.formatting;

import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.utilities.FormatUtils;
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

    @Deprecated
    public static Formatter<DateHolder> year_Old() {
        return nop();
    }

    private static Formatter<DateHolder> nop() {
        return dateHolder -> "-";
    }

    private static Formatter<Long> nop2() {
        return l -> "-";
    }

    public Formatter<DateHolder> year() {
        return new DateHolderFormatter(yearLong());
    }

    @Deprecated
    public static Formatter<Long> yearLongValue_Old() {
        return nop2();
    }

    public Formatter<Long> yearLong() {
        return new YearFormatter(config);
    }

    @Deprecated
    public static Formatter<DateHolder> day_Old() {
        return nop();
    }

    public Formatter<DateHolder> day() {
        return new DateHolderFormatter(dayLong());
    }

    public Formatter<Long> dayLong() {
        return new DayFormatter(config);
    }

    @Deprecated
    public static Formatter<DateHolder> second_Old() {
        return nop();
    }

    public Formatter<DateHolder> second() {
        return new DateHolderFormatter(secondLong());
    }

    public Formatter<Long> secondLong() {
        return new SecondFormatter(config);
    }

    @Deprecated
    public static Formatter<DateHolder> clock_Old() {
        return nop();
    }

    public Formatter<DateHolder> clock() {
        return new DateHolderFormatter(clockLong());
    }

    public Formatter<Long> clockLong() {
        return new ClockFormatter(config);
    }

    @Deprecated
    public static Formatter<DateHolder> iso8601NoClock_Old() {
        return dateHolder -> FormatUtils.formatTimeStampISO8601NoClock(dateHolder.getDate());
    }

    public Formatter<DateHolder> iso8601NoClock() {
        return new DateHolderFormatter(iso8601NoClockLong());
    }

    public Formatter<Long> iso8601NoClockLong() {
        return new ISO8601NoClockFormatter(config);
    }

    @Deprecated
    public static Formatter<Long> timeAmount_Old() {
        return nop2();
    }

    public Formatter<Long> timeAmount() {
        return new TimeAmountFormatter(config);
    }

    @Deprecated
    public static Formatter<Double> percentage_Old() {
        return value -> value >= 0 ? FormatUtils.cutDecimals(value * 100.0) + "%" : "-";
    }

    public Formatter<Double> percentage() {
        return new PercentageFormatter(decimals());
    }

    private Formatter<Double> decimals() {
        return new DecimalFormatter(config);
    }
}