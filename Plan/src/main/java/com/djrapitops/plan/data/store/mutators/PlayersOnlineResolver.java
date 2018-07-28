package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.utilities.html.graphs.line.Point;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Resolves dates into players online numbers with a help of a NavigableMap.
 * <p>
 * Time Complexity of O(n / 2) with the use of TreeMap.
 *
 * @author Rsl1122
 */
public class PlayersOnlineResolver extends TreeMap<Long, Integer> {

    public PlayersOnlineResolver(TPSMutator mutator) {
        List<Point> points = mutator.playersOnlinePoints();
        for (Point point : points) {
            double date = point.getX();
            double value = point.getY();
            put((long) date, (int) value);
        }
    }

    public Optional<Integer> getOnlineOn(long date) {
        Map.Entry<Long, Integer> entry = floorEntry(date);
        if (entry == null) {
            return Optional.empty();
        }
        return Optional.of(entry.getValue());
    }

    public boolean isServerOnline(long date, long timeLimit) {
        Long lastEntry = floorKey(date);
        return date - lastEntry < timeLimit;
    }
}