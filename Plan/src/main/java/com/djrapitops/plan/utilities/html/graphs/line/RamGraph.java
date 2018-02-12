package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.TPS;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Graph about RAM Usage gathered by TPSCountTimer.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.system.tasks.TPSCountTimer
 * @since 4.2.0
 */
public class RamGraph extends AbstractLineGraph {

    public RamGraph(List<TPS> tpsData) {
        super(turnToPoints(tpsData));
    }

    private static List<Point> turnToPoints(List<TPS> tpsData) {
        return tpsData.stream()
                .map(tps -> new Point(tps.getDate(), tps.getUsedMemory()))
                .collect(Collectors.toList());
    }
}
