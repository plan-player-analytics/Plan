package com.djrapitops.pluginbridge.plan.react;

import com.volmit.react.api.SampledType;

/**
 * Data container for React data points.
 *
 * @author Rsl1122
 */
public class ReactValue implements Comparable<ReactValue> {

    private final SampledType type;
    private final long date;
    private final double dataValue;

    public ReactValue(SampledType type, long date, double dataValue) {
        this.type = type;
        this.date = date;
        this.dataValue = dataValue;
    }

    public SampledType getType() {
        return type;
    }

    public long getDate() {
        return date;
    }

    public double getDataValue() {
        return dataValue;
    }

    @Override
    public int compareTo(ReactValue o) {
        return Long.compare(this.date, o.date);
    }
}