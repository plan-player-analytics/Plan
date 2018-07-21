package com.djrapitops.plan.utilities.html.graphs.bar;

public class Bar implements Comparable<Bar> {

    private final String label;
    private final long value;

    public Bar(String label, long value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public long getValue() {
        return value;
    }

    @Override
    public int compareTo(Bar bar) {
        return Long.compare(bar.value, this.value);
    }
}
