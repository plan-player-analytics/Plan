package main.java.com.djrapitops.plan.utilities.html.graphs.line;

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
                .map(tps -> new Point(tps.getDate(), tps.getTicksPerSecond()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);

    }

    public static List<TPS> filterTPS(List<TPS> tpsData, long nowMinusScale) {
        return tpsData.stream()
                .filter(Objects::nonNull)
                .filter(t -> t.getDate() >= nowMinusScale)
                .collect(Collectors.toList());
    }
}
