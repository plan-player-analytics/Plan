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
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author AuroraLS3
 */
public class PlayerCalendar {

    private final Formatter<Long> timeAmount;
    private final Formatter<Long> year;
    private final Formatter<Long> iso8601Formatter;
    private final Theme theme;
    private final Locale locale;
    private final TimeZone timeZone;

    private final List<Session> allSessions;
    private final long registered;

    PlayerCalendar(
            PlayerContainer container,
            Formatter<Long> timeAmount,
            Formatter<Long> year,
            Formatter<Long> iso8601Formatter,
            Theme theme,
            Locale locale,
            TimeZone timeZone
    ) {
        this.allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        this.registered = container.getValue(PlayerKeys.REGISTERED).orElse(0L);

        this.timeAmount = timeAmount;
        this.year = year;
        this.iso8601Formatter = iso8601Formatter;
        this.theme = theme;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    public List<CalendarEntry> getEntries() {
        List<CalendarEntry> entries = new ArrayList<>();

        entries.add(CalendarEntry
                .of(locale.getString(HtmlLang.LABEL_REGISTERED) + ": " + year.apply(registered),
                        registered
                ).withColor(theme.getValue(ThemeVal.LIGHT_GREEN))
        );

        Map<String, List<Session>> sessionsByDay = getSessionsByDay();

        for (Map.Entry<String, List<Session>> entry : sessionsByDay.entrySet()) {
            String day = entry.getKey();

            List<Session> sessions = entry.getValue();
            int sessionCount = sessions.size();
            long playtime = sessions.stream().mapToLong(Session::getLength).sum();

            entries.add(CalendarEntry
                    .of(locale.getString(HtmlLang.LABEL_PLAYTIME) + ": " + timeAmount.apply(playtime), day)
                    .withColor(theme.getValue(ThemeVal.GREEN))
            );
            entries.add(CalendarEntry.of(locale.getString(HtmlLang.SIDE_SESSIONS) + ": " + sessionCount, day));
        }

        long fiveMinutes = TimeUnit.MINUTES.toMillis(5L);

        for (Session session : allSessions) {
            String length = timeAmount.apply(session.getLength());
            Long start = session.getUnsafe(SessionKeys.START);
            Long end = session.getValue(SessionKeys.END).orElse(System.currentTimeMillis());

            entries.add(CalendarEntry
                    .of(locale.getString(HtmlLang.SESSION) + ": " + length,
                            start + timeZone.getOffset(start))
                    .withEnd(end + timeZone.getOffset(end))
            );

            for (PlayerKill kill : session.getPlayerKills()) {
                long time = kill.getDate();
                String victim = kill.getVictimName().orElse(kill.getVictim().toString());
                entries.add(CalendarEntry
                        .of(locale.getString(HtmlLang.KILLED) + ": " + victim, time)
                        .withEnd(time + fiveMinutes)
                        .withColor(theme.getValue(ThemeVal.RED))
                );
            }
        }

        return entries;
    }

    private Map<String, List<Session>> getSessionsByDay() {
        Map<String, List<Session>> sessionsByDay = new HashMap<>();
        for (Session session : allSessions) {
            String day = iso8601Formatter.apply(session.getDate());

            List<Session> sessionsOfDay = sessionsByDay.computeIfAbsent(day, Lists::create);
            sessionsOfDay.add(session);
        }
        return sessionsByDay;
    }
}
