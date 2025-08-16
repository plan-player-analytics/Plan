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

import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.settings.theme.ThemeVal;

import java.util.*;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author AuroraLS3
 */
public class ServerCalendar {

    private final SortedMap<Long, Integer> uniquePerDay;
    private final SortedMap<Long, Integer> newPerDay;
    private final SortedMap<Long, Integer> sessionsPerDay;
    private final SortedMap<Long, Long> playtimePerDay;

    private final Formatter<Long> iso8601TZIndependent;

    ServerCalendar(
            SortedMap<Long, Integer> uniquePerDay,
            SortedMap<Long, Integer> newPerDay,
            SortedMap<Long, Long> playtimePerDay,
            NavigableMap<Long, Integer> sessionsPerDay,
            Formatter<Long> iso8601TZIndependent
    ) {
        this.uniquePerDay = uniquePerDay;
        this.newPerDay = newPerDay;
        this.iso8601TZIndependent = iso8601TZIndependent;
        this.sessionsPerDay = sessionsPerDay;
        this.playtimePerDay = playtimePerDay;
    }

    public List<CalendarEntry> getEntries() {
        List<CalendarEntry> entries = new ArrayList<>();
        appendUniquePlayers(entries);
        appendNewPlayers(entries);
        appendSessionCounts(entries);
        appendPlaytime(entries);
        return entries;
    }

    private void appendNewPlayers(List<CalendarEntry> entries) {
        for (Map.Entry<Long, Integer> entry : newPerDay.entrySet()) {
            int newPlayers = entry.getValue();
            if (newPlayers <= 0) {
                continue;
            }

            Long key = entry.getKey();
            String day = iso8601TZIndependent.apply(key);

            entries.add(CalendarEntry.of(HtmlLang.NEW_CALENDAR.getKey(), newPlayers, day)
                    .withColor(ThemeVal.LIGHT_GREEN.getDefaultValue()));
        }
    }

    private void appendUniquePlayers(List<CalendarEntry> entries) {
        for (Map.Entry<Long, Integer> entry : uniquePerDay.entrySet()) {
            long uniquePlayers = entry.getValue();
            if (uniquePlayers <= 0) {
                continue;
            }

            Long key = entry.getKey();
            String day = iso8601TZIndependent.apply(key);

            entries.add(CalendarEntry.of(HtmlLang.UNIQUE_CALENDAR.getKey(), uniquePlayers, day));
        }
    }

    private void appendPlaytime(List<CalendarEntry> entries) {
        for (Map.Entry<Long, Long> entry : playtimePerDay.entrySet()) {
            long playtime = entry.getValue();
            if (playtime <= 0) {
                continue;
            }
            Long key = entry.getKey();
            String day = iso8601TZIndependent.apply(key);

            entries.add(CalendarEntry.of(HtmlLang.LABEL_PLAYTIME.getKey(), playtime, day)
                    .withColor(ThemeVal.GREEN.getDefaultValue()));
        }
    }

    private void appendSessionCounts(List<CalendarEntry> entries) {
        for (Map.Entry<Long, Integer> entry : sessionsPerDay.entrySet()) {
            int sessionCount = entry.getValue();
            if (sessionCount <= 0) {
                continue;
            }
            Long key = entry.getKey();
            String day = iso8601TZIndependent.apply(key);

            entries.add(CalendarEntry.of(HtmlLang.SIDE_SESSIONS.getKey(), sessionCount, day)
                    .withColor(ThemeVal.TEAL.getDefaultValue()));
        }
    }
}
