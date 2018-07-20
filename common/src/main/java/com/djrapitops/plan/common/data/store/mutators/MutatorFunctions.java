package com.djrapitops.plan.common.data.store.mutators;


import com.djrapitops.plan.common.utilities.html.graphs.line.Point;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.stream.Collectors;

public class MutatorFunctions {

    public static List<Point> toPoints(NavigableMap<Long, Integer> map) {
        return map.entrySet().stream()
                .map(entry -> new Point(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public static int average(Map<Long, Integer> map) {
        return (int) map.values().stream()
                .mapToInt(i -> i)
                .average().orElse(0);
    }

}
