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
import com.djrapitops.plan.data.store.objects.DateHolder;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;
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

    private final Formatter<Long> timeAmountFormatter;
    private final Formatter<Long> yearLongFormatter;
    private final Formatter<DateHolder> iso8601Formatter;
    private final Theme theme;

    private final List<Session> allSessions;
    private final long registered;

    PlayerCalendar(
            PlayerContainer container,
            Formatter<Long> timeAmountFormatter,
            Formatter<Long> yearLongFormatter,
            Formatter<DateHolder> iso8601Formatter,
            Theme theme
    ) {
        this.allSessions = container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>());
        this.registered = container.getValue(PlayerKeys.REGISTERED).orElse(0L);

        this.timeAmountFormatter = timeAmountFormatter;
        this.yearLongFormatter = yearLongFormatter;
        this.iso8601Formatter = iso8601Formatter;
        this.theme = theme;
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
                    .append("',color: '").append(Theme.getValue_Old(ThemeVal.GREEN)).append("'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("'}");
        }
    }

    private Map<String, List<Session>> getSessionsByDay() {
        Map<String, List<Session>> sessionsByDay = new HashMap<>();
        for (Session session : allSessions) {
            String day = iso8601Formatter.apply(session);

            List<Session> sessionsOfDay = sessionsByDay.getOrDefault(day, new ArrayList<>());
            sessionsOfDay.add(session);
            sessionsByDay.put(day, sessionsOfDay);
        }
        return sessionsByDay;
    }

    private void appendSessionsAndKills(StringBuilder series) {
        long fiveMinutes = TimeAmount.MINUTE.ms() * 5L;

        for (Session session : allSessions) {
            String length = timeAmountFormatter.apply(session.getLength());

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
