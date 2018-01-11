package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.systems.tasks.TPSCountTimer;
import com.djrapitops.plan.utilities.analysis.Point;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for creating scatter graph data from RAM Usage snapshots with TPS task.
 *
 * @author Rsl1122
 * @see TPSCountTimer
 * @since 3.6.0
 */
public class RamGraph {

    /**
     * Constructor used to hide the public constructor
     */
    private RamGraph() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a series data string from given data.
     *
     * @param tpsData TPS Data collected by TPSCountTimer, one data point for each minute.
     * @return Series data for HighCharts
     */
    public static String createSeries(List<TPS> tpsData) {
        List<Point> points = tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getUsedMemory()))
                .collect(Collectors.toList());
        return LineSeries.createSeries(points, true);
    }
}
