package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.TreeMap;

/**
 * Factory class for different objects representing HTML calendars.
 *
 * @author Rsl1122
 */
@Singleton
public class CalendarFactory {

    private final Formatter<Long> iso8601Formatter;
    private final Formatter<Long> timeAmountFormatter;
    private final Theme theme;

    @Inject
    public CalendarFactory(
            Formatters formatters,
            Theme theme
    ) {
        this.iso8601Formatter = formatters.iso8601NoClockLong();
        this.timeAmountFormatter = formatters.timeAmount();
        this.theme = theme;
    }

    public PlayerCalendar playerCalendar(PlayerContainer player) {
        return new PlayerCalendar(player);
    }

    public ServerCalendar serverCalendar(
            PlayersMutator mutator,
            TreeMap<Long, Integer> uniquePerDay,
            TreeMap<Long, Integer> newPerDay
    ) {
        return new ServerCalendar(mutator, uniquePerDay, newPerDay, iso8601Formatter, timeAmountFormatter, theme);
    }
}