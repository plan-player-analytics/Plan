/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
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

    public LineGraph diskGraph(TPSMutator mutator) {
        return new DiskGraph(mutator, config.isTrue(Settings.DISPLAY_GAPS_IN_GRAPH_DATA));
    }
}