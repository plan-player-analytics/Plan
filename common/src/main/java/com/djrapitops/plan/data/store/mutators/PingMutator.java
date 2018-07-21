package com.djrapitops.plan.data.store.mutators;


import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.CommonKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
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
        TreeMap<Long, List<Ping>> byStartOfMinute = dateMutator.groupByStartOfMinute();

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
            if (value < 0 || 8000 < value) {
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
            if (value < 0 || 8000 < value) {
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
                .filter(value -> value >= 0 && value <= 8000)
                .average().orElse(-1);
    }
}
