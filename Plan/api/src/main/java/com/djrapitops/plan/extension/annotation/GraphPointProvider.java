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
package com.djrapitops.plan.extension.annotation;

import com.djrapitops.plan.extension.FormatType;
import com.djrapitops.plan.extension.graph.Aggregates;
import com.djrapitops.plan.extension.graph.XAxisType;

import java.util.concurrent.TimeUnit;

/**
 * Defines a method that returns {@link com.djrapitops.plan.extension.graph.DataPoint}.
 * <p>
 * This method will be sampled at specific times, defined by {@link GraphPointProvider#sampleInterval()} and {@link GraphPointProvider#sampleIntervalUnit()}.
 * <p>
 * DataPoint can have multiple values per x-axis value, to provide multiple series.
 * <p>
 * You have a lot of control over how the graph is displayed with the metadata methods specific to this annotation.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 *
 * @author AuroraLS3
 */
public @interface GraphPointProvider {

    /**
     * Name of the graph that is shown to users.
     *
     * @return "Version count history"
     */
    String displayName();

    /**
     * Display-priority of the graph, highest value is placed top most.
     * <p>
     * Two values with same priority may appear in a random order.
     *
     * @return Priority between 0 and {@code Integer.MAX_VALUE}.
     */
    int priority() default 0;

    /**
     * Define what formatter to use for x-axis.
     *
     * @return by default XAxisType.DATE_MILLIS.
     */
    XAxisType xAxisType() default XAxisType.DATE_MILLIS;

    /**
     * Should the x-axis of the graph be visualized so that it starts and stops at specific value, growing if necessary.
     *
     * @return false by default
     * @see GraphPointProvider#xAxisSoftMin()
     * @see GraphPointProvider#xAxisSoftMax()
     */
    boolean xAxisSoftLimits() default false;

    /**
     * Should the y-axis of the graph be visualized so that it starts and stops at specific value, growing if necessary.
     *
     * @return false by default
     * @see GraphPointProvider#yAxisSoftMin()
     * @see GraphPointProvider#yAxisSoftMax()
     */
    boolean yAxisSoftLimits() default false;

    /**
     * Minimum for x-axis, growing if necessary.
     *
     * @return 0 by default.
     */
    int xAxisSoftMin() default 0;

    /**
     * Maximum for x-axis, growing if necessary.
     *
     * @return 2 by default.
     */
    int xAxisSoftMax() default 2;

    /**
     * Minimum for y-axis, growing if necessary.
     *
     * @return 0 by default.
     */
    int yAxisSoftMin() default 0;

    /**
     * Maximum for y-axis, growing if necessary.
     *
     * @return 2 by default.
     */
    int yAxisSoftMax() default 2;

    /**
     * Units of each series in the datapoint.
     * <p>
     * If more value series are defined than points the first unit will be applied to rest of the points.
     * <p>
     * Include null or empty string "" in the array if you want just a numeric unit.
     *
     * @return Array of unit to show in the y-axis, same unit will be collapsed to same y-axis. e.g. ["Players", "Dollaridoos", "Players"].
     */
    String[] unitNames() default {};

    /**
     * Format of each value in the datapoint.
     * <p>
     * If this method returns less format types than series is used, FormatType.NONE will be applied.
     *
     * @return [FormatType.TIME_MILLISECONDS]
     */
    FormatType[] valueFormats() default {};

    /**
     * Hex color string of each series.
     * <p>
     * Allows you to control color of your data.
     * <p>
     * If this is left unspecified, a predefined color series used in visualization will be used.
     *
     * @return ["#cccccc", "#222222"]
     */
    String[] seriesColors() default {};

    /**
     * How often this method should be called.
     *
     * @return by default returns 1.
     * @see GraphPointProvider#sampleIntervalUnit() to define unit.
     */
    int sampleInterval() default 1;

    /**
     * How often this method should be called.
     *
     * @return by default returns TimeUnit.MINUTES.
     * @see GraphPointProvider#sampleInterval() to define amount.
     */
    TimeUnit sampleIntervalUnit() default TimeUnit.MINUTES;

    /**
     * Should the values in the datapoint be stackable when graphed.
     * <p>
     * This is useful if all values represent same kind of data.
     * <p>
     * If {@link GraphPointProvider#unitNames()} or {@link GraphPointProvider#valueFormats()} return more than one unit name
     * or format, this method will be ignored, since different units or valueFormats don't stack.
     *
     * @return false by default.
     */
    boolean supportsStacking() default false;

    /**
     * Define any aggregate functions the value series supports.
     * <p>
     * Automatic aggregate numbers (As if using {@link NumberProvider}) will be added to the same {@link Tab} if these are defined.
     *
     * @return None by default.
     */
    Aggregates[] supportedAggregateFunctions() default {};

}
