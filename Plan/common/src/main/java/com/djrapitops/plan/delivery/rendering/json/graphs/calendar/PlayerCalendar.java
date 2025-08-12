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
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.PlayerKills;
import com.djrapitops.plan.settings.locale.lang.HtmlLang;
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

    private final Formatter<Long> iso8601Formatter;
    private final TimeZone timeZone;

    private final List<FinishedSession> allSessions;
    private final long registered;

    PlayerCalendar(
            PlayerContainer container,
            Formatter<Long> iso8601Formatter,
            TimeZone timeZone
    ) {
        this.allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        this.registered = container.getValue(PlayerKeys.REGISTERED).orElse(0L);

        this.iso8601Formatter = iso8601Formatter;
        this.timeZone = timeZone;
    }

    public List<CalendarEntry> getEntries() {
        List<CalendarEntry> entries = new ArrayList<>();

        entries.add(CalendarEntry
                .of(HtmlLang.LABEL_REGISTERED.getKey(), registered, registered + timeZone.getOffset(registered))
                .withColor(ThemeVal.LIGHT_GREEN.getDefaultValue())
        );

        Map<String, List<FinishedSession>> sessionsByDay = getSessionsByDay();

        for (Map.Entry<String, List<FinishedSession>> entry : sessionsByDay.entrySet()) {
            String day = entry.getKey();

            List<FinishedSession> sessions = entry.getValue();
            int sessionCount = sessions.size();
            long playtime = sessions.stream().mapToLong(FinishedSession::getLength).sum();

            entries.add(CalendarEntry
                    .of(HtmlLang.LABEL_PLAYTIME.getKey(), playtime, day)
                    .withColor(ThemeVal.GREEN.getDefaultValue())
            );
            entries.add(CalendarEntry.of(HtmlLang.SIDE_SESSIONS.getKey(), sessionCount, day)
                    .withColor(ThemeVal.TEAL.getDefaultValue()));
        }

        long fiveMinutes = TimeUnit.MINUTES.toMillis(5L);

        for (FinishedSession session : allSessions) {
            long start = session.getStart();
            long end = session.getEnd();

            entries.add(CalendarEntry
                    .of(HtmlLang.SESSION.getKey(), session.getLength(), start + timeZone.getOffset(start))
                    .withEnd(end + timeZone.getOffset(end))
                    .withColor(ThemeVal.TEAL.getDefaultValue())
            );

            for (PlayerKill kill : session.getExtraData(PlayerKills.class).map(PlayerKills::asList).orElseGet(ArrayList::new)) {
                long time = kill.getDate();
                String victim = kill.getVictim().getName();
                entries.add(CalendarEntry
                        .of(HtmlLang.KILLED.getKey(), victim, time)
                        .withEnd(time + fiveMinutes)
                        .withColor(ThemeVal.RED.getDefaultValue())
                );
            }
        }

        return entries;
    }

    private Map<String, List<FinishedSession>> getSessionsByDay() {
        Map<String, List<FinishedSession>> sessionsByDay = new HashMap<>();
        for (FinishedSession session : allSessions) {
            String day = iso8601Formatter.apply(session.getDate());

            List<FinishedSession> sessionsOfDay = sessionsByDay.computeIfAbsent(day, Lists::create);
            sessionsOfDay.add(session);
        }
        return sessionsByDay;
    }
}
