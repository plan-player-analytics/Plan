/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.rendering.json.graphs.line.Point;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Resolves dates into players online numbers with a help of a NavigableMap.
 * <p>
 * Time Complexity of O(n / 2) with the use of TreeMap.
 *
 * @author AuroraLS3
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

    public int findLonelyJoins(List<Long> joinDates) {
        int lonely = 0;
        for (Long joinDate : joinDates) {
            if (getOnlineOn(joinDate).orElse(-1) == 0) lonely++;
        }
        return lonely;
    }

    public boolean isServerOnline(long date, long timeLimit) {
        Long lastEntry = floorKey(date);
        return date - lastEntry < timeLimit;
    }
}