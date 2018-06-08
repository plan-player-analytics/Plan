package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.CommonKeys;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plugin.utilities.Verify;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mutator for a list of Sessions.
 * <p>
 * Can be used to get properties of a large number of sessions easily.
 *
 * @author Rsl1122
 */
public class SessionsMutator {

    private List<Session> sessions;

    public static SessionsMutator forContainer(DataContainer dataContainer) {
        Verify.isTrue(dataContainer.supports(CommonKeys.SESSIONS),
                () -> new IllegalArgumentException("Given DataContainer does not support SESSIONS-key"));
        return new SessionsMutator(dataContainer.getValue(CommonKeys.SESSIONS).orElse(new ArrayList<>()));
    }

    public static SessionsMutator copyOf(SessionsMutator mutator) {
        return new SessionsMutator(mutator.sessions);
    }

    public SessionsMutator(List<Session> sessions) {
        this.sessions = sessions;
    }

    public SessionsMutator filterSessionsBetween(long after, long before) {
        sessions = sessions.stream()
                .filter(session -> session.getSessionEnd() >= after && session.getSessionStart() <= before)
                .collect(Collectors.toList());
        return this;
    }

    public WorldTimes toTotalWorldTimes() {
        WorldTimes total = new WorldTimes(new HashMap<>());

        for (Session session : sessions) {
            WorldTimes worldTimes = session.getWorldTimes();
            total.add(worldTimes);
        }

        return total;
    }

    public List<PlayerKill> toPlayerKillList() {
        return sessions.stream()
                .map(Session::getPlayerKills)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public int toMobKillCount() {
        return sessions.stream()
                .mapToInt(Session::getMobKills)
                .sum();
    }

    public int toDeathCount() {
        return sessions.stream()
                .mapToInt(Session::getDeaths)
                .sum();
    }

    public long toPlaytime() {
        return sessions.stream()
                .mapToLong(Session::getLength)
                .sum();
    }

    public long toAfkTime() {
        return sessions.stream()
                .mapToLong(Session::getAfkLength)
                .sum();
    }

    public long toActivePlaytime() {
        return sessions.stream()
                .mapToLong(Session::getActiveLength)
                .sum();
    }

    public long toLastSeen() {
        return sessions.stream()
                .mapToLong(session -> Math.max(session.getSessionStart(), session.getSessionEnd()))
                .max().orElse(-1);
    }

    public long toLongestSessionLength() {
        OptionalLong longestSession = sessions.stream().mapToLong(Session::getLength).max();
        if (longestSession.isPresent()) {
            return longestSession.getAsLong();
        }
        return -1;
    }

    public long toAverageSessionLength() {
        OptionalDouble average = sessions.stream().map(Session::getLength)
                .mapToLong(i -> i)
                .average();
        if (average.isPresent()) {
            return (long) average.getAsDouble();
        }
        return 0L;
    }

    public long toMedianSessionLength() {
        List<Long> sessionLengths = sessions.stream().map(Session::getLength)
                .sorted()
                .collect(Collectors.toList());
        if (sessionLengths.isEmpty()) {
            return 0;
        }
        return sessionLengths.get(sessionLengths.size() / 2);
    }

    public int count() {
        return sessions.size();
    }

    public int toPlayerKillCount() {
        return toPlayerKillList().size();
    }
}