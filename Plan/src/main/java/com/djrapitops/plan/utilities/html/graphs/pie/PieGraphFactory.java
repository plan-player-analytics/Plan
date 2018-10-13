package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.WorldAliasSettings;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Factory class for different objects representing HTML pie graphs.
 *
 * @author Rsl1122
 */
@Singleton
public class PieGraphFactory {

    private final PlanConfig config;
    private final Theme theme;

    @Inject
    public PieGraphFactory(
            PlanConfig config,
            Theme theme
    ) {
        this.config = config;
        this.theme = theme;
    }

    public Pie activityPie(Map<String, Set<UUID>> activityData) {
        String[] colors = theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        return new ActivityPie(activityData, colors);
    }

    public Pie serverPreferencePie(Map<UUID, String> serverNames, Map<UUID, WorldTimes> serverWorldTimes) {
        return new ServerPreferencePie(serverNames, serverWorldTimes);
    }

    public WorldPie worldPie(WorldTimes worldTimes) {
        WorldAliasSettings worldAliasSettings = config.getWorldAliasSettings();
        Map<String, Long> playtimePerAlias = worldAliasSettings.getPlaytimePerAlias(worldTimes);
        Map<String, GMTimes> gmTimesPerAlias = worldAliasSettings.getGMTimesPerAlias(worldTimes);
        String[] colors = theme.getValue(ThemeVal.GRAPH_WORLD_PIE).split(", ");
        boolean orderByPercentage = config.isTrue(Settings.ORDER_WORLD_PIE_BY_PERC);
        return new WorldPie(playtimePerAlias, gmTimesPerAlias, colors, orderByPercentage);
    }
}