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
package com.djrapitops.plan.utilities.html.graphs.pie;

import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.config.WorldAliasSettings;
import com.djrapitops.plan.system.settings.paths.DisplaySettings;
import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public Pie activityPie_old(Map<String, Set<UUID>> activityData) {
        String[] colors = theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(", ");
        Map<String, Integer> flatActivityData = activityData.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size()));
        return new ActivityPie(flatActivityData, colors);
    }

    public Pie activityPie(Map<String, Integer> activityData) {
        String[] colors = Arrays.stream(theme.getValue(ThemeVal.GRAPH_ACTIVITY_PIE).split(","))
                .map(color -> color.trim().replace("\"", ""))
                .toArray(String[]::new);
        return new ActivityPie(activityData, colors);
    }

    public Pie serverPreferencePie(Map<UUID, String> serverNames, Map<UUID, WorldTimes> serverWorldTimes) {
        return new ServerPreferencePie(serverNames, serverWorldTimes);
    }

    public Pie serverPreferencePie(Map<String, Long> serverPlaytimes) {
        return new ServerPreferencePie(serverPlaytimes);
    }

    public WorldPie worldPie(WorldTimes worldTimes) {
        WorldAliasSettings worldAliasSettings = config.getWorldAliasSettings();
        Map<String, Long> playtimePerAlias = worldAliasSettings.getPlaytimePerAlias(worldTimes);
        Map<String, GMTimes> gmTimesPerAlias = worldAliasSettings.getGMTimesPerAlias(worldTimes);
        String[] colors = Arrays.stream(theme.getValue(ThemeVal.GRAPH_WORLD_PIE).split(","))
                .map(color -> color.trim().replace("\"", ""))
                .toArray(String[]::new);
        boolean orderByPercentage = config.isTrue(DisplaySettings.ORDER_WORLD_PIE_BY_PERC);
        return new WorldPie(playtimePerAlias, gmTimesPerAlias, colors, orderByPercentage);
    }
}