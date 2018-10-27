package com.djrapitops.plan.utilities.analysis;

import java.util.Collections;
import java.util.List;

/**
 * Math utility for calculating the median from Integer values.
 *
 * @author Rsl1122
 */
public class Median<T extends Number & Comparable<? super T>> {

    private final List<T> values;
    private int size;

    private Median(List<T> values) {
        this.values = values;
        Collections.sort(values);
        size = values.size();
    }

    public static <T extends Number & Comparable<? super T>> Median<T> forList(List<T> list) {
        return new Median<>(list);
    }

    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        list.sort(null);
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
        double x1 = values.get(half).doubleValue();
        double x2 = values.get(half - 1).doubleValue();
        return (x1 + x2) / 2;
    }

    private double calculateOdd() {
        int half = size / 2;
        return values.get(half).doubleValue();
    }
}