package main.java.com.djrapitops.plan.utilities.html.graphs.line;

import main.java.com.djrapitops.plan.data.container.TPS;
import main.java.com.djrapitops.plan.systems.tasks.TPSCountTimer;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from Chunk and Entity load snapshots with TPS task.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 3.6.0
 */
public class WorldLoadGraphCreator {

    /**
     * Constructor used to hide the public constructor
     */
    private WorldLoadGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates series graph data of entity load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String buildSeriesDataStringEntities(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getEntityCount()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }

    /**
     * Creates series data of chunk load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String buildSeriesDataStringChunks(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getChunksLoaded()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }
}
