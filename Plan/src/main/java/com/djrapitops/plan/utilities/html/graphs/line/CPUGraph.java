package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Graph about CPU Usage gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.tasks.TPSCountTimer
 * @since 4.2.0
 */
public class CPUGraph extends AbstractLineGraph {

    public CPUGraph(List<TPS> tpsData) {
        super(transformToPoints(tpsData));
    }

    private static List<Point> transformToPoints(List<TPS> tpsData) {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getCPUUsage()))
                .collect(Collectors.toList());
    }
}
