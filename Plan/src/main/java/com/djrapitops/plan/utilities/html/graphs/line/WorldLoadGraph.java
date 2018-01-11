package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.systems.tasks.TPSCountTimer;
import com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from Chunk and Entity load snapshots with TPS task.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 3.6.0
 */
public class WorldLoadGraph {

    /**
     * Constructor used to hide the public constructor
     */
    private WorldLoadGraph() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates series graph data of entity load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String createSeriesEntities(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getEntityCount()))
                .collect(Collectors.toList());
        return LineSeries.createSeries(points, true);
    }

    /**
     * Creates series data of chunk load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String createSeriesChunks(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getChunksLoaded()))
                .collect(Collectors.toList());
        return LineSeries.createSeries(points, true);
    }
}
