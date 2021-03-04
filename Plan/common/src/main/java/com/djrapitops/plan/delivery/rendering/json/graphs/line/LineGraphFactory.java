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
package com.djrapitops.plan.delivery.rendering.json.graphs.line;

import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.gathering.domain.Ping;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.utilities.comparators.PointComparator;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Factory class for different objects representing HTML line graphs.
 *
 * @author AuroraLS3
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
        return lineGraph(points, shouldDisplayGapsInData());
    }

    public LineGraph lineGraph(List<Point> points, boolean displayGaps) {
        points.sort(new PointComparator());
        return new LineGraph(points, displayGaps);
    }

    public LineGraph lineGraph(List<Point> points, LineGraph.GapStrategy gapStrategy) {
        points.sort(new PointComparator());
        return new LineGraph(points, gapStrategy);
    }

    public LineGraph chunkGraph(TPSMutator mutator) {
        return new ChunkGraph(mutator, shouldDisplayGapsInData());
    }

    public LineGraph cpuGraph(TPSMutator mutator) {
        return new CPUGraph(mutator, shouldDisplayGapsInData());
    }

    public LineGraph entityGraph(TPSMutator mutator) {
        return new EntityGraph(mutator, shouldDisplayGapsInData());
    }

    public LineGraph playersOnlineGraph(TPSMutator mutator) {
        return new PlayersOnlineGraph(mutator, shouldDisplayGapsInData());
    }

    public PingGraph pingGraph(List<Ping> pingList) {
        return new PingGraph(pingList, shouldDisplayGapsInData());
    }

    public LineGraph ramGraph(TPSMutator mutator) {
        return new RamGraph(mutator, shouldDisplayGapsInData());
    }

    public LineGraph tpsGraph(TPSMutator mutator) {
        return new TPSGraph(mutator, shouldDisplayGapsInData());
    }

    public LineGraph diskGraph(TPSMutator mutator) {
        return new DiskGraph(mutator, shouldDisplayGapsInData());
    }

    private boolean shouldDisplayGapsInData() {
        return config.isTrue(DisplaySettings.GAPS_IN_GRAPH_DATA);
    }
}