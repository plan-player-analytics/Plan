/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.utilities.FormatUtils;

import java.util.*;

/**
 * Utility for creating FullCalendar calendar event array on Player page.
 *
 * @author Rsl1122
 */
public class ServerCalendar {

    private final List<Long> registerDates;
    private final Map<UUID, List<Session>> sessions;

    public ServerCalendar(List<Long> registerDates, Map<UUID, List<Session>> sessions) {
        this.registerDates = registerDates;
        this.sessions = sessions;
    }

    public String toCalendarSeries() {
        StringBuilder series = new StringBuilder("[");

        series.append("{title: 'badcode',start:0}");
        appendSessionRelatedData(series);
        appendRegistered(series);

        return series.append("]").toString();
    }

    private void appendRegistered(StringBuilder series) {
        Map<String, Integer> registeredByDay = getRegisteredByDay();

        for (Map.Entry<String, Integer> entry : registeredByDay.entrySet()) {
            String day = entry.getKey();
            Integer newPlayers = entry.getValue();

            series.append(",{title: 'New: ").append(newPlayers)
                    .append("',start:'").append(day)
                    .append("',color: '#8BC34A'")
                    .append("}");
        }

    }

    private void appendSessionRelatedData(StringBuilder series) {
        Map<String, Map<UUID, List<Session>>> sessionsByDay = getSessionsByDay();

        for (Map.Entry<String, Map<UUID, List<Session>>> entry : sessionsByDay.entrySet()) {
            String day = entry.getKey();

            Map<UUID, List<Session>> sessionsPerUsers = entry.getValue();
            long sessionCount = sessionsPerUsers.values().stream().mapToLong(Collection::size).sum();
            long playtime = sessionsPerUsers.values().stream().flatMap(Collection::stream).mapToLong(Session::getLength).sum();
            long uniquePlayers = sessionsPerUsers.size();

            series.append(",{title: 'Playtime: ").append(FormatUtils.formatTimeAmount(playtime))
                    .append("',start:'").append(day)
                    .append("',color: '#4CAF50'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("',color: '#009688'")
                    .append("}");

            series.append(",{title: 'Unique: ").append(uniquePlayers)
                    .append("',start:'").append(day)
                    .append("'}");
        }
    }

    private Map<String, Map<UUID, List<Session>>> getSessionsByDay() {
        Map<String, Map<UUID, List<Session>>> sessionsByDay = new HashMap<>();
        for (Map.Entry<UUID, List<Session>> entry : sessions.entrySet()) {
            UUID player = entry.getKey();
            List<Session> sessions = entry.getValue();

            for (Session session : sessions) {
                String day = FormatUtils.formatTimeStampISO8601NoClock(session.getSessionStart());

                Map<UUID, List<Session>> sessionsPerUserOfDay = sessionsByDay.getOrDefault(day, new HashMap<>());
                List<Session> sessionsOfUser = sessionsPerUserOfDay.getOrDefault(player, new ArrayList<>());
                sessionsOfUser.add(session);
                sessionsPerUserOfDay.put(player, sessionsOfUser);
                sessionsByDay.put(day, sessionsPerUserOfDay);
            }
        }
        return sessionsByDay;
    }

    private Map<String, Integer> getRegisteredByDay() {
        Map<String, Integer> RegisteredByDay = new HashMap<>();
        for (Long registered : registerDates) {
            String day = FormatUtils.formatTimeStampISO8601NoClock(registered);

            int registeredPerDay = RegisteredByDay.getOrDefault(day, 0);
            registeredPerDay += 1;
            RegisteredByDay.put(day, registeredPerDay);
        }
        return RegisteredByDay;
    }
}