package com.djrapitops.plan.data.container;


import com.djrapitops.plan.data.store.objects.DateObj;

import java.util.UUID;

public class Ping extends DateObj<Double> {

    private final UUID serverUUID;
    private final double average;
    private final int min;
    private final int max;

    public Ping(long date, UUID serverUUID, int min, int max, double average) {
        super(date, average);
        this.serverUUID = serverUUID;
        this.average = average;
        this.min = min;
        this.max = max;
    }

    public UUID getServerUUID() {
        return serverUUID;
    }

    public double getAverage() {
        return average;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }
}
