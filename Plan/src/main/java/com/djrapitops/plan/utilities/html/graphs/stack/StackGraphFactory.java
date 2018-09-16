package com.djrapitops.plan.utilities.html.graphs.stack;

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import com.djrapitops.plan.utilities.formatting.Formatter;
import com.djrapitops.plan.utilities.formatting.Formatters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

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
        String[] colors = theme.getThemeValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        return new ActivityStackGraph(activityData, colors, dayFormatter);
    }
}