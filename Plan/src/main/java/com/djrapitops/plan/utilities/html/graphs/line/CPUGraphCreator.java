package main.java.com.djrapitops.plan.utilities.html.graphs.line;

import main.java.com.djrapitops.plan.data.container.TPS;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

public class CPUGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private CPUGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String buildSeriesDataString(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }
}
