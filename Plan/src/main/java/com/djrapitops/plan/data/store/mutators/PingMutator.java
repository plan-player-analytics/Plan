package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.containers.DataContainer;
import com.djrapitops.plan.data.store.keys.CommonKeys;

import java.util.ArrayList;
import java.util.List;

public class PingMutator {

    private final List<Ping> pings;

    public PingMutator(List<Ping> pings) {
        this.pings = pings;
    }

    public static PingMutator forContainer(DataContainer container) {
        return new PingMutator(container.getValue(CommonKeys.PING).orElse(new ArrayList<>()));
    }
}
