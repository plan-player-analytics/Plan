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

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author Rsl1122
 */
public class PlayerCalendar {

    private final Formatter<Long> timeAmountFormatter;
    private final Formatter<Long> yearLongFormatter;
    private final Formatter<Long> iso8601Formatter;
    private final Theme theme;
    private final TimeZone timeZone;

    private final List<Session> allSessions;
    private final long registered;

    PlayerCalendar(
            PlayerContainer container,
            Formatter<Long> timeAmountFormatter,
            Formatter<Long> yearLongFormatter,
            Formatter<Long> iso8601Formatter,
            Theme theme,
            TimeZone timeZone
    ) {
        this.allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        this.registered = container.getValue(PlayerKeys.REGISTERED).orElse(0L);

        this.timeAmountFormatter = timeAmountFormatter;
        this.yearLongFormatter = yearLongFormatter;
        this.iso8601Formatter = iso8601Formatter;
        this.theme = theme;
        this.timeZone = timeZone;
    }

    public String toCalendarSeries() {
        StringBuilder series = new StringBuilder("[");

        appendRegister(series);
        appendDailyPlaytime(series);
        appendSessionsAndKills(series);

        return series.append("]").toString();
    }

    private void appendDailyPlaytime(StringBuilder series) {
        Map<String, List<Session>> sessionsByDay = getSessionsByDay();

        for (Map.Entry<String, List<Session>> entry : sessionsByDay.entrySet()) {
            String day = entry.getKey();

            List<Session> sessions = entry.getValue();
            int sessionCount = sessions.size();
            long playtime = sessions.stream().mapToLong(Session::getLength).sum();

            series.append(",{title: 'Playtime: ").append(timeAmountFormatter.apply(playtime))
                    .append("',start:'").append(day)
                    .append("',color: '").append(theme.getValue(ThemeVal.GREEN)).append("'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("'}");
        }
    }

    private Map<String, List<Session>> getSessionsByDay() {
        Map<String, List<Session>> sessionsByDay = new HashMap<>();
        for (Session session : allSessions) {
            String day = iso8601Formatter.apply(session.getDate());

            List<Session> sessionsOfDay = sessionsByDay.getOrDefault(day, new ArrayList<>());
            sessionsOfDay.add(session);
            sessionsByDay.put(day, sessionsOfDay);
        }
        return sessionsByDay;
    }

    private void appendSessionsAndKills(StringBuilder series) {
        long fiveMinutes = TimeUnit.MINUTES.toMillis(5L);

        for (Session session : allSessions) {
            String length = timeAmountFormatter.apply(session.getLength());
            Long start = session.getUnsafe(SessionKeys.START);
            Long end = session.getValue(SessionKeys.END).orElse(System.currentTimeMillis());

            series.append(",{title: 'Session: ").append(length)
                    .append("',start:").append(start + timeZone.getOffset(start))
                    .append(",end:").append(end + timeZone.getOffset(end))
                    .append("}");

            for (PlayerKill kill : session.getPlayerKills()) {
                long time = kill.getDate();

                series.append(",{title: 'Killed: ").append(kill.getVictimName().orElse(kill.getVictim().toString()))
                        .append("',start:").append(time)
                        .append(",end:").append(time + fiveMinutes)
                        .append(",color: '").append(theme.getValue(ThemeVal.RED)).append("'")
                        .append("}");
            }
        }
    }

    private void appendRegister(StringBuilder series) {
        series.append("{title: 'Registered: ").append(yearLongFormatter.apply(registered)).append("'," +
                "start: ").append(this.registered).append(",color: '").append(theme.getValue(ThemeVal.LIGHT_GREEN)).append("'}");
    }
}
