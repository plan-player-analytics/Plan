package com.djrapitops.plan.utilities.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Math utility for calculating the median from Integer values.
 *
 * @author Rsl1122
 */
public class Median {

    private final List<Long> values;
    private int size;

    private Median(Collection<Integer> values, int b) {
        this(values.stream().map(i -> (long) i).collect(Collectors.toList()));
    }

    private Median(List<Long> values) {
        this.values = values;
        Collections.sort(values);
        size = values.size();
    }

    public static Median forInt(Collection<Integer> integers) {
        return new Median(integers, 0);
    }

    public static Median forLong(List<Long> longs) {
        return new Median(longs);
    }

    public double calculate() {
        if (values.isEmpty()) {
            return -1;
        }
        if (size % 2 == 0) {
            return calculateEven();
        } else {
            return calculateOdd();
        }
    }

    private double calculateEven() {
        int half = size / 2;
        double x1 = values.get(half);
        double x2 = values.get(half - 1);
        return (x1 + x2) / 2;
    }

    private double calculateOdd() {
        int half = size / 2;
        return (double) values.get(half);
    }
}