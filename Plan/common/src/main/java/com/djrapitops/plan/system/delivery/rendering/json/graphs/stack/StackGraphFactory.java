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
package com.djrapitops.plan.system.delivery.rendering.json.graphs.stack;

import com.djrapitops.plan.system.delivery.domain.DateMap;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * Factory class for different objects representing HTML stack graphs.
 *
 * @author Rsl1122
 */
@Singleton
public class StackGraphFactory {

    private final Theme theme;
    private final Formatter<Long> dayFormatter;

    @Inject
    public StackGraphFactory(
            Formatters formatters,
            Theme theme
    ) {
        this.theme = theme;
        this.dayFormatter = formatters.dayLong();
    }

    public StackGraph activityStackGraph(TreeMap<Long, Map<String, Set<UUID>>> activityData) {
        String[] colors = theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        return new ActivityStackGraph(activityData, colors, dayFormatter);
    }

    public StackGraph activityStackGraph(DateMap<Map<String, Integer>> activityData) {
        String[] colors = Arrays.stream(theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(","))
                .map(color -> color.trim().replace("\"", ""))
                .toArray(String[]::new);
        return new ActivityStackGraph(activityData, colors, dayFormatter);
    }
}