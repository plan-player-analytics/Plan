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

/**
 * Aggregates that a graph supports.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 *
 * @author AuroraLS3
 */
public enum Aggregates {

    /**
     * SUM that can be filtered by time value x, eg. last 30 days
     */
    SUM_OVER_TIME,
    /**
     * MEAN (average) that can be filtered by time value x, eg. last 30 days
     */
    MEAN_OVER_TIME,
    /**
     * MIN that can be filtered by time value x, eg. last 30 days
     */
    MIN_OVER_TIME,
    /**
     * MAX that can be filtered by time value x, eg. last 30 days
     */
    MAX_OVER_TIME,
    /**
     * Cumulative SUM over all points
     */
    SUM_TOTAL,
    /**
     * MEAN (average) over all points
     */
    MEAN_TOTAL,
    /**
     * Minimum seen point.
     */
    MIN_TOTAL,
    /**
     * Maximum seen point.
     */
    MAX_TOTAL,

}
