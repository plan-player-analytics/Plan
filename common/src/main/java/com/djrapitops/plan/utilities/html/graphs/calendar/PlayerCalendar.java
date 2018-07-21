/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.data.store.mutators.formatting.Formatter;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author Rsl1122
 */
public class PlayerCalendar {

    private final List<Session> allSessions;
    private final long registered;

    public PlayerCalendar(PlayerContainer container) {
        this(
                container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>()),
                container.getValue(PlayerKeys.REGISTERED).orElse(0L)
        );
    }

    private PlayerCalendar(List<Session> allSessions, long registered) {
        this.allSessions = allSessions;
        this.registered = registered;
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

            series.append(",{title: 'Playtime: ").append(Formatters.timeAmount().apply(playtime))
                    .append("',start:'").append(day)
                    .append("',color: '").append(Theme.getValue(ThemeVal.GREEN)).append("'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("'}");
        }
    }

    private Map<String, List<Session>> getSessionsByDay() {
        Map<String, List<Session>> sessionsByDay = new HashMap<>();
        for (Session session : allSessions) {
            String day = Formatters.iso8601NoClock().apply(session);

            List<Session> sessionsOfDay = sessionsByDay.getOrDefault(day, new ArrayList<>());
            sessionsOfDay.add(session);
            sessionsByDay.put(day, sessionsOfDay);
        }
        return sessionsByDay;
    }

    private void appendSessionsAndKills(StringBuilder series) {
        long fiveMinutes = TimeAmount.MINUTE.ms() * 5L;

        Formatter<Long> timeFormatter = Formatters.timeAmount();
        for (Session session : allSessions) {
            String length = timeFormatter.apply(session.getLength());

            series.append(",{title: 'Session: ").append(length)
                    .append("',start:").append(session.getUnsafe(SessionKeys.START))
                    .append(",end:").append(session.getValue(SessionKeys.END)
                    .orElse(System.currentTimeMillis()))
                    .append("}");

            for (PlayerKill kill : session.getUnsafe(SessionKeys.PLAYER_KILLS)) {
                long time = kill.getDate();

                series.append(",{title: 'Killed: ").append(kill.getVictim())
                        .append("',start:").append(time)
                        .append(",end:").append(time + fiveMinutes)
                        .append(",color: '").append(Theme.getValue(ThemeVal.RED)).append("'")
                        .append("}");
            }
        }
    }

    private void appendRegister(StringBuilder series) {
        String registered = FormatUtils.formatTimeStampYear(this.registered);

        series.append("{title: 'Registered: ").append(registered).append("'," +
                "start: ").append(this.registered).append(",color: '").append(Theme.getValue(ThemeVal.LIGHT_GREEN)).append("'}");
    }
}
