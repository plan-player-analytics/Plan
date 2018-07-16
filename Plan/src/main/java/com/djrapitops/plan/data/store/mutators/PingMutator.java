package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.CommonKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PingMutator {

    private final List<Ping> pings;

    public PingMutator(List<Ping> pings) {
        this.pings = pings;
    }

    public static PingMutator forContainer(DataContainer container) {
        return new PingMutator(container.getValue(CommonKeys.PING).orElse(new ArrayList<>()));
    }

    public Optional<Ping> max() {
        Ping maxPing = null;
        int max = 0;
        for (Ping ping : pings) {
            Integer value = ping.getMax();
            if (value > max) {
                max = value;
                maxPing = ping;
            }
        }

        return Optional.ofNullable(maxPing);
    }

    public double average() {
        return pings.stream().mapToDouble(Ping::getAverage).average().orElse(-1);
    }
}
