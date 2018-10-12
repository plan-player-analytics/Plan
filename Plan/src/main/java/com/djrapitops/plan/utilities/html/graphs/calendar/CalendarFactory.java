package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.system.settings.theme.Theme;
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
    private final Theme theme;
    private final Formatters formatters;

    @Inject
    public CalendarFactory(
            Formatters formatters,
            Theme theme
    ) {
        this.formatters = formatters;
        this.theme = theme;
    }

    public PlayerCalendar playerCalendar(PlayerContainer player) {
        return new PlayerCalendar(
                player,
                formatters.timeAmount(), formatters.yearLong(), formatters.iso8601NoClock(), theme
        );
    }

    public ServerCalendar serverCalendar(
            PlayersMutator mutator,
            TreeMap<Long, Integer> uniquePerDay,
            TreeMap<Long, Integer> newPerDay
    ) {
        return new ServerCalendar(
                mutator, uniquePerDay, newPerDay,
                formatters.iso8601NoClockLong(), formatters.timeAmount(), theme
        );
    }
}