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

import com.djrapitops.plan.extension.graph.HistoryStrategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows you to return a {@code DataPoint[]} for an existing {@link GraphPointProvider}.
 * <p>
 * This method will be called according to {@link com.djrapitops.plan.extension.CallEvents}.
 * <p>
 * This is useful if you have recorded history of your data.
 * <p>
 * If you want to use this method to provide more than one datapoint at a time, you should keep track of when the last
 * time this method was called was, and then only return data between that point in time, and now.
 * <p>
 * Requires capability DATA_EXTENSION_GRAPH_API.
 *
 * Does not support {@link Conditional}.
 *
 * @author AuroraLS3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface GraphHistoryPointsProvider {

    /**
     * Name of the method with {@link GraphPointProvider} annotation that this method is giving history for.
     *
     * @return "valuesByTime"
     */
    String methodName();

    /**
     * Defines a strategy the data will be appended to existing data.
     * <p>
     * You only need to change this if your data changes while server is offline and unable to sample it
     * <p>
     * {@link HistoryStrategy#REPLACE_CHANGED_VALUES} also replaces matching data.
     *
     * @return {@link HistoryStrategy#ONLY_APPEND_MISSING} by default.
     */
    HistoryStrategy strategy() default HistoryStrategy.ONLY_APPEND_MISSING;

}
