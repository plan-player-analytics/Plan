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

/**
 * This is a LineGraph for any set of Points, thus it is Abstract.
 *
 * @author Rsl1122
 */
public class LineGraph implements HighChart {

    private final boolean displayGaps;
    private final List<Point> points;

    public LineGraph(List<Point> points, boolean displayGaps) {
        this.points = points;
        this.displayGaps = displayGaps;
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

            if (displayGaps && lastX != null && date - lastX > TimeUnit.MINUTES.toMillis(3L)) {
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
        if (displayGaps) {
            return MutatorFunctions.addMissing(points, TimeUnit.MINUTES.toMillis(1L), null);
        }
        return points;
    }

    private void addMissingPoints(StringBuilder arrayBuilder, Long lastX, long date) {
        long iterate = lastX + TimeUnit.MINUTES.toMillis(1L);
        while (iterate < date) {
            arrayBuilder.append("[").append(iterate).append(",null],");
            iterate += TimeUnit.MINUTES.toMillis(30L);
        }
    }
}
