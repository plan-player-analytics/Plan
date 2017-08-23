package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class PlayerActivityGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private PlayerActivityGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    public static String buildSeriesDataString(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getPlayers()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }
}
