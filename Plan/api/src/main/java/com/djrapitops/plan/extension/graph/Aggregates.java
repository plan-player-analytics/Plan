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
package com.djrapitops.plan.extension.graph;

import com.djrapitops.plan.extension.annotation.GraphProvider;

/**
 * Aggregates that a graph supports.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 *
 * @author AuroraLS3
 */
public enum Aggregates {

    /**
     * Maximum seen point.
     * <p>
     * Produces a data point.
     */
    MAX_PEAK,
    /**
     * Minimum seen point.
     * <p>
     * Produces a data point.
     */
    MIN_VALLEY,
    /**
     * MAX that can be filtered by time value x, eg. last 30 days
     * <p>
     * Produces a data point.
     */
    MAX_PEAK_OVER_TIME,
    /**
     * MIN that can be filtered by time value x, eg. last 30 days
     * <p>
     * Produces a data point.
     */
    MIN_VALLEY_OVER_TIME,
    /**
     * SUM that can be filtered by time value x, eg. last 30 days
     * <p>
     * Produces a data point.
     */
    SUM_OVER_TIME,
    /**
     * Cumulative SUM over all points
     * <p>
     * Produces a data point.
     */
    SUM_ALL,
    /**
     * SUM over all player graphs points into a single server graph - Only used with {@link PlayerGraphDataSource}.
     * <p>
     * Granularity of the sum is {@link GraphProvider#sampleInterval()}.
     * <p>
     * Produces a graph on server page.
     */
    SUM_INTO_GRAPH

}
