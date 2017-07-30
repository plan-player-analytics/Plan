package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 * @since 3.5.0
 */
public class TPSGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private TPSGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String buildSeriesDataString(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getTps()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);

    }

    public static List<TPS> filterTPS(List<TPS> tpsData, long nowMinusScale) {
        return tpsData.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getDate() >= nowMinusScale)
                .collect(Collectors.toList());
    }
}
