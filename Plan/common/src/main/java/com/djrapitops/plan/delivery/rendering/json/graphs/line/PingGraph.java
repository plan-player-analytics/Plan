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

import com.djrapitops.plan.gathering.domain.Ping;

import java.util.ArrayList;
import java.util.List;

public class PingGraph {

    private final LineGraph maxGraph;
    private final LineGraph minGraph;
    private final LineGraph avgGraph;

    /**
     * Constructor.
     *
     * @param pings       List of Ping values:
     *                    List should be filtered so that only a single entry for each date exists.
     * @param displayGaps Should data gaps be displayed.
     */
    PingGraph(List<Ping> pings, boolean displayGaps) {
        List<Point> max = new ArrayList<>();
        List<Point> min = new ArrayList<>();
        List<Point> avg = new ArrayList<>();

        for (Ping ping : pings) {
            long date = ping.getDate();

            max.add(new Point(date, ping.getMax()));
            min.add(new Point(date, ping.getMin()));
            avg.add(new Point(date, ping.getAverage()));
        }

        maxGraph = new LineGraph(max, displayGaps);
        minGraph = new LineGraph(min, displayGaps);
        avgGraph = new LineGraph(avg, displayGaps);
    }

    public LineGraph getMaxGraph() {
        return maxGraph;
    }

    public LineGraph getMinGraph() {
        return minGraph;
    }

    public LineGraph getAvgGraph() {
        return avgGraph;
    }
}
