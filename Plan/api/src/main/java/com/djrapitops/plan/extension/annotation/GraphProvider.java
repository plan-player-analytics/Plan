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

import com.djrapitops.plan.extension.graph.Aggregates;
import com.djrapitops.plan.extension.graph.HistoryStrategy;
import com.djrapitops.plan.extension.graph.ServerGraphDataSource;
import com.djrapitops.plan.extension.graph.XAxisType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * Defines a method that returns {@link com.djrapitops.plan.extension.graph.DataPoint}.
 * <p>
 * METHODS ANNOTATED BY THIS WILL BE TREATED CASE INSENSITIVE (all lowercase) to store data in the database.
 * <p>
 * The DataSource returned by this will sampled at specific times, defined by {@link GraphProvider#sampleInterval()} and {@link GraphProvider#sampleIntervalUnit()}.
 * <p>
 * DataPoint can have multiple values per x-axis value, to provide multiple series.
 * <p>
 * You have a lot of control over how the graph is displayed with the metadata returned by the DataSource and properties in this annotation.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 * <p>
 * Does not support {@link Conditional}.
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GraphProvider {

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
     * How often this method should be called.
     * <p>
     * For PlayerGraphDataSource minimum is 30 SECONDS.
     * =120 points per hour per player,
     * with 100 players =12000 points/hour
     * <p>
     * Storage requirements can be calculated with (points per hour) * (players) * 64 * (2 + y)
     * where y is amount of values returned with DataPoint.
     * y=1: 120 * 100 * 64 * (2 + 1) = 2.3 MB of data/hour = 17.3 days to fill up 1 GB.
     * <p>
     * Be considerate, try to aim for at least 30 days/1 GB.
     * <p>
     * For ServerGraphDataSource minimum is 5 SECONDS. = 720 points per hour
     * <p>
     * Recommended interval is 1 MINUTE or bigger, due to data storage size.
     *
     * @return by default returns 1.
     * @see GraphProvider#sampleIntervalUnit() to define unit.
     */
    int sampleInterval() default 1;

    /**
     * How often this method should be called.
     *
     * @return by default returns TimeUnit.MINUTES.
     * @see GraphProvider#sampleInterval() to define amount.
     */
    TimeUnit sampleIntervalUnit() default TimeUnit.MINUTES;

    /**
     * Should the values in the datapoint be stackable when graphed.
     * <p>
     * This is useful if all values represent same kind of data.
     * <p>
     * If {@link ServerGraphDataSource#getSeriesMetadata()} returns more than one unique unit name
     * or format, this method will be ignored, since different units or valueFormats don't stack.
     *
     * @return false by default.
     */
    boolean supportsStacking() default false;

    /**
     * Define any aggregate functions the value series supports.
     * <p>
     * Automatic aggregate numbers (As if using {@link NumberProvider}) will be added to the same {@link Tab} if these are defined.
     * <p>
     * If the graph has multiple series (multiple y values) there will be no aggregates.
     *
     * @return None by default.
     */
    Aggregates[] supportedAggregateFunctions() default {};


    /**
     * Defines a {@link HistoryStrategy} the data will be appended to existing data.
     *
     * @return {@link HistoryStrategy#NO_HISTORY} by default.
     */
    HistoryStrategy strategy() default HistoryStrategy.NO_HISTORY;
}
