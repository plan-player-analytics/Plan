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
package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.TimeSettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.SortedMap;
import java.util.TimeZone;

/**
 * Factory class for different objects representing HTML calendars.
 *
 * @author Rsl1122
 */
@Singleton
public class CalendarFactory {
    private final Theme theme;
    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public CalendarFactory(
            PlanConfig config,
            Formatters formatters,
            Theme theme
    ) {
        this.config = config;
        this.formatters = formatters;
        this.theme = theme;
    }

    public PlayerCalendar playerCalendar(PlayerContainer player) {
        return new PlayerCalendar(
                player,
                formatters.timeAmount(), formatters.yearLong(), formatters.iso8601NoClockLong(), theme,
                config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT")
        );
    }

    public ServerCalendar serverCalendar(
            SortedMap<Long, Integer> uniquePerDay,
            SortedMap<Long, Integer> newPerDay
    ) {
        return new ServerCalendar(
                uniquePerDay, newPerDay,
                formatters.iso8601NoClockLong(), formatters.timeAmount(), theme,
                config.get(TimeSettings.USE_SERVER_TIME) ? TimeZone.getDefault() : TimeZone.getTimeZone("GMT")
        );
    }
}