package com.djrapitops.plan.utilities.html.graphs.line;

import com.djrapitops.plan.data.container.Ping;
import com.djrapitops.plan.data.store.mutators.TPSMutator;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Factory class for different objects representing HTML line graphs.
 *
 * @author Rsl1122
 */
@Singleton
public class LineGraphFactory {

    private final PlanConfig config;

    @Inject
    public LineGraphFactory(
            PlanConfig config
    ) {
        this.config = config;
    }

    public LineGraph lineGraph(List<Point> points) {
        return new LineGraph(points, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph chunkGraph(TPSMutator mutator) {
        return new ChunkGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph cpuGraph(TPSMutator mutator) {
        return new CPUGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph entityGraph(TPSMutator mutator) {
        return new EntityGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph playersOnlineGraph(TPSMutator mutator) {
        return new PlayersOnlineGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public PingGraph pingGraph(List<Ping> pingList) {
        return new PingGraph(pingList, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph ramGraph(TPSMutator mutator) {
        return new RamGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }

    public LineGraph tpsGraph(TPSMutator mutator) {
        return new TPSGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }
}