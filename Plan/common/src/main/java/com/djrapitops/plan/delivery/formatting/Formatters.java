/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.formatting;

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.formatting.time.*;
import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory for new instances of different {@link Formatter}s.
 *
 * @author AuroraLS3
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
    private final HttpLastModifiedDateFormatter httpLastModifiedDateFormatter;
    private final JavascriptDateFormatter javascriptDateFormatter;
    private final ISO8601NoClockFormatter iso8601NoClockLongFormatter;
    private final ISO8601NoClockTZIndependentFormatter iso8601NoClockTZIndependentFormatter;

    private final TimeAmountFormatter timeAmountFormatter;

    private final DecimalFormatter decimalFormatter;
    private final PercentageFormatter percentageFormatter;
    private final ByteSizeFormatter byteSizeFormatter;

    @Inject
    public Formatters(PlanConfig config, Locale locale) {
        yearLongFormatter = new YearFormatter(config, locale);
        dayLongFormatter = new DayFormatter(config, locale);
        clockLongFormatter = new ClockFormatter(config, locale);
        secondLongFormatter = new SecondFormatter(config, locale);
        httpLastModifiedDateFormatter = new HttpLastModifiedDateFormatter(config, locale);
        javascriptDateFormatter = new JavascriptDateFormatter(config, locale);
        iso8601NoClockLongFormatter = new ISO8601NoClockFormatter(config, locale);
        iso8601NoClockTZIndependentFormatter = new ISO8601NoClockTZIndependentFormatter();

        yearFormatter = new DateHolderFormatter(yearLongFormatter);
        dayFormatter = new DateHolderFormatter(dayLongFormatter);
        secondFormatter = new DateHolderFormatter(secondLongFormatter);
        clockFormatter = new DateHolderFormatter(clockLongFormatter);
        iso8601NoClockFormatter = new DateHolderFormatter(iso8601NoClockLongFormatter);

        timeAmountFormatter = new TimeAmountFormatter(config);

        decimalFormatter = new DecimalFormatter(config);
        percentageFormatter = new PercentageFormatter(decimalFormatter);
        byteSizeFormatter = new ByteSizeFormatter(decimalFormatter);

        Formatters.Holder.set(this);
    }

    public static Formatters getInstance() {
        return Holder.formatters.get();
    }

    public static void clearSingleton() {
        Holder.formatters.set(null);
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

    public Formatter<Long> httpLastModifiedLong() {
        return httpLastModifiedDateFormatter;
    }

    public Formatter<Long> javascriptDateFormatterLong() {
        return javascriptDateFormatter;
    }

    public Formatter<Long> iso8601NoClockLong() {
        return iso8601NoClockLongFormatter;
    }

    public Formatter<Long> iso8601NoClockTZIndependentLong() {
        return iso8601NoClockTZIndependentFormatter;
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

    public Formatter<Double> byteSize() {
        return byteSizeFormatter;
    }

    public Formatter<Long> byteSizeLong() {
        return value -> byteSizeFormatter.apply((double) value);
    }

    public Formatter<Long> getNumberFormatter(FormatType type) {
        switch (type) {
            case DATE_SECOND:
                return secondLong();
            case DATE_YEAR:
                return yearLong();
            case TIME_MILLISECONDS:
                return timeAmount();
            case NONE:
            default:
                return Object::toString;
        }
    }

    static class Holder {
        static final AtomicReference<Formatters> formatters = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(Formatters service) {
            Formatters.Holder.formatters.set(service);
        }
    }
}