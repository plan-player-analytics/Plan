package main.java.com.djrapitops.plan.utilities.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from RAM Usage snapshots with TPS task.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 3.6.0
 */
public class RamGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private RamGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a series data string from given data.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String buildSeriesDataString(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getUsedMemory()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }
}
