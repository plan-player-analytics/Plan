package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
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

    public static String buildScatterDataString(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> points = tpsData.stream()
                .filter(tps -> tps.getDate() >= now - scale)
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }
}
