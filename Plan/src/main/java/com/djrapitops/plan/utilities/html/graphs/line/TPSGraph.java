package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSGraph {

    /**
     * Constructor used to hide the public constructor
     */
    private TPSGraph() {
        throw new IllegalStateException("Utility class");
    }

    public static String createSeries(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getTicksPerSecond()))
                .collect(Collectors.toList());
        return LineSeries.createSeries(points, true);

    }
}
