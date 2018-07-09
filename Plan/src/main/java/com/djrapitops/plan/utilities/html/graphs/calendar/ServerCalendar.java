/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.utilities.html.graphs.calendar;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.PlayerContainer;
import com.djrapitops.plan.data.store.keys.PlayerKeys;
import com.djrapitops.plan.data.store.mutators.PlayersMutator;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
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

    public ServerCalendar(PlayersMutator mutator) {
        registerDates = new ArrayList<>();
        sessions = new HashMap<>();

        for (PlayerContainer container : mutator.all()) {
            UUID uuid = container.getUnsafe(PlayerKeys.UUID);
            registerDates.add(container.getValue(PlayerKeys.REGISTERED).orElse(0L));
            sessions.put(uuid, container.getValue(PlayerKeys.SESSIONS).orElse(new ArrayList<>()));
        }
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
                    .append("',color: '").append(Theme.getValue(ThemeVal.LIGHT_GREEN)).append("'")
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

            series.append(",{title: 'Playtime: ").append(Formatters.timeAmount().apply(playtime))
                    .append("',start:'").append(day)
                    .append("',color: '").append(Theme.getValue(ThemeVal.GREEN)).append("'")
                    .append("}");

            series.append(",{title: 'Sessions: ").append(sessionCount)
                    .append("',start:'").append(day)
                    .append("',color: '").append(Theme.getValue(ThemeVal.TEAL)).append("'")
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
                String day = Formatters.iso8601NoClock().apply(session);

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
        Map<String, Integer> registeredByDay = new HashMap<>();
        for (Long registered : registerDates) {
            String day = FormatUtils.formatTimeStampISO8601NoClock(registered);

            int registeredPerDay = registeredByDay.getOrDefault(day, 0);
            registeredPerDay += 1;
            registeredByDay.put(day, registeredPerDay);
        }
        return registeredByDay;
    }
}
