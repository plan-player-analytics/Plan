package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from Chunk & Entity load snapshots with TPS task.
 *
 * @author Rsl1122
 * @since 3.6.0
 * @see main.java.com.djrapitops.plan.data.listeners.TPSCountTimer
 */
public class WorldLoadGraphCreator {

    public static String buildSeriesDataStringEntities(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getEntityCount()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }

    public static String buildSeriesDataStringChunks(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getChunksLoaded()))
                .collect(Collectors.toList());
        return SeriesCreator.seriesGraph(points, true);
    }

    /**
     * Constructor used to hide the public constructor
     */
    private WorldLoadGraphCreator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates scatter graph data of entity load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @param scale Time span this graph resides within. (Milliseconds)
     * @return Scatter Graph data string for ChartJs
     */
    public static String buildScatterDataStringEntities(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> entityPoints = tpsData.stream()
                .filter(tps -> tps.getDate() >= now - scale)
                .map(tps -> new Point(tps.getDate(), tps.getEntityCount()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(entityPoints, true);
    }

    /**
     * Creates scatter graph data of chunk load.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @param scale Time span this graph resides within. (Milliseconds)
     * @return Scatter Graph data string for ChartJs
     */
    public static String buildScatterDataStringChunks(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> chunkPoints = tpsData.stream()
                .filter(tps -> tps.getDate() >= now - scale)
                .map(tps -> new Point(tps.getDate(), tps.getChunksLoaded()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(chunkPoints, true);
    }
}
