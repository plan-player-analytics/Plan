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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.CommonKeys;
import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.utilities.Predicates;
import com.djrapitops.plan.utilities.comparators.DateHolderOldestComparator;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.*;
import java.util.function.Predicate;

public class PingMutator {

    private final List<Ping> pings;

    public PingMutator(List<Ping> pings) {
        this.pings = pings;
    }

    public static PingMutator forContainer(DataContainer container) {
        return new PingMutator(container.getValue(CommonKeys.PING).orElse(Collections.emptyList()));
    }

    public PingMutator filterBy(Predicate<Ping> predicate) {
        return new PingMutator(Lists.filter(pings, predicate));
    }

    public PingMutator filterByServer(UUID serverUUID) {
        return filterBy(ping -> serverUUID.equals(ping.getServerUUID()));
    }

    public PingMutator mutateToByMinutePings() {
        DateHoldersMutator<Ping> dateMutator = new DateHoldersMutator<>(pings);
        SortedMap<Long, List<Ping>> byStartOfMinute = dateMutator.groupByStartOfMinute();

        return new PingMutator(Lists.map(byStartOfMinute.entrySet(), entry -> {
            PingMutator mutator = new PingMutator(entry.getValue());

            return new Ping(entry.getKey(), null,
                    mutator.min(), mutator.max(), mutator.average());
        }));
    }

    public static Map<UUID, SortedMap<Long, Ping>> sortByServers(List<Ping> pings) {
        Map<UUID, SortedMap<Long, Ping>> sorted = new HashMap<>();
        for (Ping ping : pings) {
            UUID serverUUID = ping.getServerUUID();
            SortedMap<Long, Ping> serverSessions = sorted.getOrDefault(serverUUID, new TreeMap<>());
            serverSessions.put(ping.getDate(), ping);
            sorted.put(serverUUID, serverSessions);
        }
        return sorted;
    }

    public void addPingToSessions(List<Session> sessions) {
        if (sessions.isEmpty()) return;

        Comparator<DateHolder> comparator = new DateHolderOldestComparator();
        sessions.sort(comparator);
        pings.sort(comparator);
        Map<UUID, SortedMap<Long, Ping>> pingByServer = sortByServers(pings);
        Map<UUID, List<Session>> sessionsByServer = SessionsMutator.sortByServers(sessions);
        for (Map.Entry<UUID, SortedMap<Long, Ping>> entry : pingByServer.entrySet()) {
            UUID serverUUID = entry.getKey();
            SortedMap<Long, Ping> pingOfServer = entry.getValue();
            if (pingOfServer.isEmpty()) continue;

            List<Session> sessionsOfServer = sessionsByServer.getOrDefault(serverUUID, Collections.emptyList());
            double pingCount = 0.0;
            int pingEntries = 0;

            for (Session session : sessionsOfServer) {
                long start = session.getDate();
                Long end = session.getValue(SessionKeys.END).orElseGet(System::currentTimeMillis);
                if (end < start) continue;
                // Calculate average ping for each session with a Ping submap
                SortedMap<Long, Ping> duringSession = pingOfServer.subMap(start, end);
                for (Ping ping : duringSession.values()) {
                    pingCount += ping.getAverage();
                    pingEntries++;
                }
                if (pingEntries != 0) {
                    session.putRawData(SessionKeys.AVERAGE_PING, pingCount / pingEntries);
                }
                pingCount = 0.0;
                pingEntries = 0;
            }
        }
    }

    public List<Ping> all() {
        return pings;
    }

    public int max() {
        int max = -1;
        for (Ping ping : pings) {
            Integer value = ping.getMax();
            if (value <= 0 || 4000 < value) {
                continue;
            }
            if (value > max) {
                max = value;
            }
        }

        return max;
    }

    public int min() {
        int min = -1;
        for (Ping ping : pings) {
            Integer value = ping.getMin();
            if (value <= 0 || 4000 < value) {
                continue;
            }
            if (value < min || min == -1) {
                min = value;
            }
        }

        return min;
    }

    public double average() {
        return pings.stream().mapToDouble(Ping::getAverage)
                .filter(Predicates::pingInRange)
                .average().orElse(-1);
    }
}
