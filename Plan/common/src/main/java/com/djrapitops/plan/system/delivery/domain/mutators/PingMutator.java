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
package com.djrapitops.plan.system.delivery.domain.mutators;

import com.djrapitops.plan.system.delivery.domain.container.DataContainer;
import com.djrapitops.plan.system.delivery.domain.keys.CommonKeys;
import com.djrapitops.plan.system.gathering.domain.Ping;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PingMutator {

    private final List<Ping> pings;

    public PingMutator(List<Ping> pings) {
        this.pings = pings;
    }

    public static PingMutator forContainer(DataContainer container) {
        return new PingMutator(container.getValue(CommonKeys.PING).orElse(new ArrayList<>()));
    }

    public PingMutator filterBy(Predicate<Ping> predicate) {
        return new PingMutator(pings.stream().filter(predicate).collect(Collectors.toList()));
    }

    public PingMutator filterByServer(UUID serverUUID) {
        return filterBy(ping -> serverUUID.equals(ping.getServerUUID()));
    }

    public PingMutator mutateToByMinutePings() {
        DateHoldersMutator<Ping> dateMutator = new DateHoldersMutator<>(pings);
        SortedMap<Long, List<Ping>> byStartOfMinute = dateMutator.groupByStartOfMinute();

        return new PingMutator(byStartOfMinute.entrySet().stream()
                .map(entry -> {
                    PingMutator mutator = new PingMutator(entry.getValue());

                    return new Ping(entry.getKey(), null,
                            mutator.min(), mutator.max(), mutator.average());
                }).collect(Collectors.toList()));
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
                .filter(value -> value > 0 && value <= 4000)
                .average().orElse(-1);
    }
}
