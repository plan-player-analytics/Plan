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
package com.djrapitops.plan.delivery.rendering.json.graphs.stack;

import com.djrapitops.plan.delivery.domain.DateMap;
import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Factory class for different objects representing HTML stack graphs.
 *
 * @author AuroraLS3
 */
@Singleton
public class StackGraphFactory {

    private final Locale locale;
    private final Theme theme;
    private final Formatter<Long> dayFormatter;

    @Inject
    public StackGraphFactory(
            Locale locale,
            Formatters formatters,
            Theme theme
    ) {
        this.locale = locale;
        this.theme = theme;
        this.dayFormatter = formatters.dayLong();
    }

    public StackGraph activityStackGraph(DateMap<Map<String, Integer>> activityData) {
        String[] colors = theme.getPieColors(ThemeVal.GRAPH_ACTIVITY_PIE);
        return new ActivityStackGraph(activityData, colors, ActivityIndex.getDefaultGroupLangKeys());
    }
}