package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.utilities.html.graphs.line.Point;

import java.util.*;

/**
 * Resolves dates into players online numbers with a help of a NavigableMap.
 * <p>
 * Time Complexity of O(n / 2) with the use of TreeMap.
 *
 * @author Rsl1122
 */
public class PlayersOnlineResolver {

    private final NavigableMap<Long, Integer> onlineNumberMap;

    public PlayersOnlineResolver(TPSMutator mutator) {
        List<Point> points = mutator.playersOnlinePoints();
        onlineNumberMap = new TreeMap<>();
        for (Point point : points) {
            double date = point.getX();
            double value = point.getY();
            onlineNumberMap.put((long) date, (int) value);
        }
    }

    public Optional<Integer> getOnlineOn(long date) {
        Map.Entry<Long, Integer> entry = onlineNumberMap.floorEntry(date);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getValue());
    }

    public boolean isServerOnline(long date, long timeLimit) {
        Long lastEntry = onlineNumberMap.floorKey(date);
        return date - lastEntry < timeLimit;
    }
}