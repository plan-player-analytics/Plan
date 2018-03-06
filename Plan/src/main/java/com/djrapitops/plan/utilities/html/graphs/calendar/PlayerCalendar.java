/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
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

    public PlayerCalendar(List<Session> allSessions, long registered) {
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

            series.append(",{title: 'Playtime: ").append(FormatUtils.formatTimeAmount(playtime))
                    .append("',start:'").append(day)
                    .append("',color: '#4CAF50'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("'}");
        }
    }

    private Map<String, List<Session>> getSessionsByDay() {
        Map<String, List<Session>> sessionsByDay = new HashMap<>();
        for (Session session : allSessions) {
            String day = FormatUtils.formatTimeStampISO8601NoClock(session.getSessionStart());

            List<Session> sessionsOfDay = sessionsByDay.getOrDefault(day, new ArrayList<>());
            sessionsOfDay.add(session);
            sessionsByDay.put(day, sessionsOfDay);
        }
        return sessionsByDay;
    }

    private void appendSessionsAndKills(StringBuilder series) {
        long fiveMinutes = TimeAmount.MINUTE.ms() * 5L;
        for (Session session : allSessions) {
            String length = FormatUtils.formatTimeAmount(session.getLength());

            series.append(",{title: 'Session: ").append(length)
                    .append("',start:").append(session.getSessionStart())
                    .append(",end:").append(session.getSessionEnd())
                    .append("}");

            for (PlayerKill kill : session.getPlayerKills()) {
                long time = kill.getTime();

                series.append(",{title: 'Killed: ").append(kill.getVictim())
                        .append("',start:").append(time)
                        .append(",end:").append(time + fiveMinutes)
                        .append(",color: 'red'")
                        .append("}");
            }
        }
    }

    private void appendRegister(StringBuilder series) {
        String registered = FormatUtils.formatTimeStampYear(this.registered);

        series.append("{title: 'Registered: ").append(registered).append("'," +
                "start: ").append(this.registered).append(",color: '#8BC34A'}");
    }
}