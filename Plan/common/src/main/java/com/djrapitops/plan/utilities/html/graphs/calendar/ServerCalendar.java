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

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.SessionsMutator;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.*;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author Rsl1122
 */
public class ServerCalendar {

    private final PlayersMutator mutator;
    private final TreeMap<Long, Integer> uniquePerDay;
    private final TreeMap<Long, Integer> newPerDay;

    private final Formatter<Long> iso8601Formatter;
    private final Formatter<Long> timeAmountFormatter;
    private final Theme theme;
    private final TimeZone timeZone;

    ServerCalendar(
            PlayersMutator mutator, TreeMap<Long, Integer> uniquePerDay, TreeMap<Long, Integer> newPerDay,
            Formatter<Long> iso8601Formatter,
            Formatter<Long> timeAmountFormatter,
            Theme theme,
            TimeZone timeZone
    ) {
        this.mutator = mutator;
        this.uniquePerDay = uniquePerDay;
        this.newPerDay = newPerDay;
        this.iso8601Formatter = iso8601Formatter;
        this.timeAmountFormatter = timeAmountFormatter;
        this.theme = theme;
        this.timeZone = timeZone;
    }

    public String toCalendarSeries() {
        StringBuilder series = new StringBuilder("[");

        series.append("{\"title\": \"badcode\",\"start\":0}");
        appendSessionRelatedData(series);
        appendRegistered(series);

        return series.append("]").toString();
    }

    private void appendRegistered(StringBuilder series) {
        Map<String, Integer> registeredByDay = getRegisteredByDay();

        for (Map.Entry<String, Integer> entry : registeredByDay.entrySet()) {
            Integer newPlayers = entry.getValue();
            if (newPlayers <= 0) {
                continue;
            }

            String day = entry.getKey();

            series.append(",{\"title\": \"New: ").append(newPlayers)
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.LIGHT_GREEN)).append('"')
                    .append("}");
        }

    }

    private void appendSessionRelatedData(StringBuilder series) {
        SessionsMutator sessionsMutator = new SessionsMutator(mutator.getSessions());
        // Adds a timezone offset
        SortedMap<Long, List<Session>> byStartOfDay = sessionsMutator.toDateHoldersMutator().groupByStartOfDay(timeZone);

        // Has a timezone offset
        for (Map.Entry<Long, Integer> entry : uniquePerDay.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }

            Long key = entry.getKey();
            String day = iso8601Formatter.apply(key - timeZone.getOffset(entry.getKey()));// Remove the timezone offset since Calendar uses UTC
            List<Session> sessions = byStartOfDay.getOrDefault(key, new ArrayList<>());

            SessionsMutator dayMutator = new SessionsMutator(sessions);
            long sessionCount = dayMutator.count();
            long playtime = dayMutator.toPlaytime();
            long uniquePlayers = entry.getValue();

            series.append(",{\"title\": \"Playtime: ").append(timeAmountFormatter.apply(playtime))
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.GREEN)).append('"')
                    .append("}");

            series.append(",{\"title\": \"Sessions: ").append(sessionCount)
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.TEAL)).append('"')
                    .append("}");

            series.append(",{\"title\": \"Unique: ").append(uniquePlayers)
                    .append("\",\"start\":\"").append(day)
                    .append("\"}");
        }
    }

    private Map<String, Integer> getRegisteredByDay() {
        Map<String, Integer> registeredByDay = new HashMap<>();
        for (Map.Entry<Long, Integer> entry : newPerDay.entrySet()) {
            String day = iso8601Formatter.apply(entry.getKey());
            registeredByDay.put(day, entry.getValue());
        }
        return registeredByDay;
    }
}
