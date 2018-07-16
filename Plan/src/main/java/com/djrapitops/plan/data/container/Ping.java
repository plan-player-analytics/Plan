package com.djrapitops.plan.data.container;

import com.djrapitops.plan.data.store.objects.DateObj;

import java.util.UUID;

public class Ping extends DateObj<Integer> {

    private UUID serverUUID;

    public Ping(long date, Integer value, UUID serverUUID) {
        super(date, value);
        this.serverUUID = serverUUID;
    }

    public Ping(long date, Integer value) {
        super(date, value);
    }

    public UUID getServerUUID() {
        return serverUUID;
    }
}
