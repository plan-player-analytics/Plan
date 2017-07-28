package main.java.com.djrapitops.plan.ui.html.graphs;

import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from RAM Usage snapshots with TPS task.
 *
 * @author Rsl1122
 * @since 3.6.0
 * @see main.java.com.djrapitops.plan.data.listeners.TPSCountTimer
 */
public class RamGraphCreator {

    /**
     * Creates a scatter data string from given data.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @param scale Time span this graph resides within. (Milliseconds)
     * @return Scatter Graph data string for ChartJs
     */
    public static String buildScatterDataString(List<TPS> tpsData, long scale) {
        long now = MiscUtils.getTime();
        List<Point> points = tpsData.stream()
                .filter(tps -> tps.getDate() >= now - scale)
                .map(tps -> new Point(tps.getDate(), tps.getUsedMemory()))
                .collect(Collectors.toList());
        return ScatterGraphCreator.scatterGraph(points, true);
    }
}
