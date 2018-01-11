package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

public class CPUGraph {

    /**
     * Constructor used to hide the public constructor
     */
    private CPUGraph() {
        throw new IllegalStateException("Utility class");
    }

    public static String createSeries(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .collect(Collectors.toList());
        return LineSeries.createSeries(points, true);
    }
}
