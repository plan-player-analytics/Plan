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
package com.djrapitops.plan.delivery.rendering.json.graphs.calendar;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.settings.config.PlanConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.NavigableMap;
import java.util.SortedMap;

/**
 * Factory class for different objects representing HTML calendars.
 *
 * @author AuroraLS3
 */
@Singleton
public class CalendarFactory {
    private final PlanConfig config;
    private final Formatters formatters;

    @Inject
    public CalendarFactory(
            PlanConfig config,
            Formatters formatters
    ) {
        this.config = config;
        this.formatters = formatters;
    }

    public PlayerCalendar playerCalendar(PlayerContainer player) {
        return new PlayerCalendar(
                player,
                formatters.iso8601NoClockLong(),
                config.getTimeZone()
        );
    }

    public ServerCalendar serverCalendar(
            SortedMap<Long, Integer> uniquePerDay,
            SortedMap<Long, Integer> newPerDay,
            SortedMap<Long, Long> playtimePerDay,
            NavigableMap<Long, Integer> sessionsPerDay
    ) {
        return new ServerCalendar(
                uniquePerDay, newPerDay, playtimePerDay, sessionsPerDay,
                formatters.iso8601NoClockTZIndependentLong()
        );
    }
}