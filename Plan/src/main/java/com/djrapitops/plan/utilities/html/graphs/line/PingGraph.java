package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.Ping;

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

    public String toMaxSeries() {
        return maxGraph.toHighChartsSeries();
    }

    public String toMinSeries() {
        return minGraph.toHighChartsSeries();
    }

    public String toAvgSeries() {
        return avgGraph.toHighChartsSeries();
    }
}
