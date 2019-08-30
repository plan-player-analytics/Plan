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

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TimeZone;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author Rsl1122
 */
public class ServerCalendar {

    private final SortedMap<Long, Integer> uniquePerDay;
    private final SortedMap<Long, Integer> newPerDay;
    private final SortedMap<Long, Integer> sessionsPerDay;
    private final SortedMap<Long, Long> playtimePerDay;

    private final Formatter<Long> iso8601Formatter;
    private final Formatter<Long> timeAmountFormatter;
    private final Theme theme;
    private final TimeZone timeZone;

    ServerCalendar(
            SortedMap<Long, Integer> uniquePerDay,
            SortedMap<Long, Integer> newPerDay,
            SortedMap<Long, Long> playtimePerDay,
            NavigableMap<Long, Integer> sessionsPerDay,
            Formatter<Long> iso8601Formatter,
            Formatter<Long> timeAmountFormatter,
            Theme theme,
            TimeZone timeZone
    ) {
        this.uniquePerDay = uniquePerDay;
        this.newPerDay = newPerDay;
        this.iso8601Formatter = iso8601Formatter;
        this.timeAmountFormatter = timeAmountFormatter;
        this.sessionsPerDay = sessionsPerDay;
        this.playtimePerDay = playtimePerDay;
        this.theme = theme;
        this.timeZone = timeZone;
    }

    public String toCalendarSeries() {
        StringBuilder series = new StringBuilder("[");

        series.append("{\"title\": \"badcode\",\"start\":0}");
        appendTimeZoneOffsetData(series);

        return series.append("]").toString();
    }

    private void appendTimeZoneOffsetData(StringBuilder series) {
        // Has a timezone offset
        appendUniquePlayers(series);
        appendNewPlayers(series);
        appendSessionCounts(series);
        appendPlaytime(series);
    }

    private void appendNewPlayers(StringBuilder series) {
        for (Map.Entry<Long, Integer> entry : newPerDay.entrySet()) {
            int newPlayers = entry.getValue();
            if (newPlayers <= 0) {
                continue;
            }

            Long key = entry.getKey();
            String day = iso8601Formatter.apply(key - timeZone.getOffset(key));// Remove the timezone offset since Calendar uses UTC

            series.append(",{\"title\": \"New: ").append(newPlayers)
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.LIGHT_GREEN)).append('"')
                    .append("}");
        }
    }

    private void appendUniquePlayers(StringBuilder series) {
        for (Map.Entry<Long, Integer> entry : uniquePerDay.entrySet()) {
            long uniquePlayers = entry.getValue();
            if (uniquePlayers <= 0) {
                continue;
            }

            Long key = entry.getKey();
            String day = iso8601Formatter.apply(key - timeZone.getOffset(key));// Remove the timezone offset since Calendar uses UTC

            series.append(",{\"title\": \"Unique: ").append(uniquePlayers)
                    .append("\",\"start\":\"").append(day)
                    .append("\"}");

        }
    }

    private void appendPlaytime(StringBuilder series) {
        for (Map.Entry<Long, Long> entry : playtimePerDay.entrySet()) {
            long playtime = entry.getValue();
            if (playtime <= 0) {
                continue;
            }
            Long key = entry.getKey();
            String day = iso8601Formatter.apply(key - timeZone.getOffset(key));// Remove the timezone offset since Calendar uses UTC

            series.append(",{\"title\": \"Playtime: ").append(timeAmountFormatter.apply(playtime))
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.GREEN)).append('"')
                    .append("}");
        }
    }

    private void appendSessionCounts(StringBuilder series) {
        for (Map.Entry<Long, Integer> entry : sessionsPerDay.entrySet()) {
            int sessionCount = entry.getValue();
            if (sessionCount <= 0) {
                continue;
            }
            Long key = entry.getKey();
            String day = iso8601Formatter.apply(key - timeZone.getOffset(key));// Remove the timezone offset since Calendar uses UTC

            series.append(",{\"title\": \"Sessions: ").append(sessionCount)
                    .append("\",\"start\":\"").append(day)
                    .append("\",\"color\": \"").append(theme.getValue(ThemeVal.TEAL)).append('"')
                    .append("}");
        }
    }
}
