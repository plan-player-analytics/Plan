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
package com.djrapitops.plan.delivery.rendering.json.graphs.line;

import com.djrapitops.plan.delivery.domain.mutators.MutatorFunctions;
import com.djrapitops.plan.delivery.rendering.json.graphs.HighChart;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * This is a LineGraph for any set of Points, thus it is Abstract.
 *
 * @author AuroraLS3
 */
public class LineGraph implements HighChart {

    private final List<Point> points;
    private final GapStrategy gapStrategy;

    public LineGraph(List<Point> points, boolean displayGaps) {
        this(points, new GapStrategy(
                displayGaps,
                TimeUnit.MINUTES.toMillis(3),  // Acceptable gap
                TimeUnit.MINUTES.toMillis(1),  // To first filler
                TimeUnit.MINUTES.toMillis(30), // Filler frequency
                null
        ));
    }

    public LineGraph(List<Point> points, GapStrategy gapStrategy) {
        this.points = points;
        this.gapStrategy = gapStrategy;
    }

    @Override
    public String toHighChartsSeries() {
        StringBuilder arrayBuilder = new StringBuilder("[");

        int size = points.size();
        Long lastX = null;
        for (int i = 0; i < size; i++) {
            Point point = points.get(i);
            Double y = point.getY();
            long date = (long) point.getX();

            if (gapStrategy.fillGaps && lastX != null && date - lastX > gapStrategy.acceptableGapMs) {
                addMissingPoints(arrayBuilder, lastX, date);
            }
            lastX = date;

            arrayBuilder.append("[").append(date).append(",").append(y).append("]");
            if (i < size - 1) {
                arrayBuilder.append(",");
            }
        }

        arrayBuilder.append("]");
        return arrayBuilder.toString();
    }

    public List<Point> getPoints() {
        if (gapStrategy.fillGaps) {
            return MutatorFunctions.addMissing(points, gapStrategy);
        }
        return points;
    }

    public List<Number[]> getPointArrays() {
        return getPoints().stream().map(Point::toArray).collect(Collectors.toList());
    }

    private void addMissingPoints(StringBuilder arrayBuilder, Long lastX, long date) {
        long iterate = lastX + gapStrategy.diffToFirstGapPointMs;
        while (iterate < date) {
            arrayBuilder.append("[").append(iterate).append(",").append(gapStrategy.fillWith).append("],");
            iterate += gapStrategy.fillFrequencyMs;
        }
    }

    public static class GapStrategy {
        public final boolean fillGaps;
        public final long acceptableGapMs;
        public final long diffToFirstGapPointMs;
        public final long fillFrequencyMs;
        public final Double fillWith;

        /**
         * Create a GapStrategy.
         *
         * @param fillGaps              true/false, should the gaps in data be filled with something?
         * @param acceptableGapMs       How many milliseconds is acceptable between points before filling in points.
         * @param diffToFirstGapPointMs How many milliseconds to last data point are added to add first filler point.
         * @param fillFrequencyMs       How many milliseconds should be added after each filler point.
         * @param fillWith              Data value for the fill, null for no data, value for some data.
         */
        public GapStrategy(
                boolean fillGaps,
                long acceptableGapMs,
                long diffToFirstGapPointMs,
                long fillFrequencyMs,
                Double fillWith
        ) {
            this.fillGaps = fillGaps;
            this.acceptableGapMs = acceptableGapMs;
            this.diffToFirstGapPointMs = diffToFirstGapPointMs;
            this.fillFrequencyMs = fillFrequencyMs;
            this.fillWith = fillWith;
        }
    }
}
