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
package com.djrapitops.plan.delivery.rendering.json.graphs.pie;

import com.djrapitops.plan.delivery.domain.mutators.ActivityIndex;
import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.gathering.domain.WorldTimes;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.WorldAliasSettings;
import com.djrapitops.plan.settings.config.paths.DisplaySettings;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.GenericLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

/**
 * Factory class for different objects representing HTML pie graphs.
 *
 * @author AuroraLS3
 */
@Singleton
public class PieGraphFactory {

    private final PlanConfig config;
    private final Locale locale;
    private final Theme theme;

    @Inject
    public PieGraphFactory(
            PlanConfig config,
            Locale locale,
            Theme theme
    ) {
        this.config = config;
        this.locale = locale;
        this.theme = theme;
    }

    public Pie activityPie(Map<String, Integer> activityData) {
        String[] colors = theme.getDefaultPieColors(ThemeVal.GRAPH_ACTIVITY_PIE);
        return new ActivityPie(activityData, colors, ActivityIndex.getDefaultGroupLangKeys());
    }

    public Pie serverPreferencePie(Map<ServerUUID, String> serverNames, Map<ServerUUID, WorldTimes> serverWorldTimes) {
        return new ServerPreferencePie(serverNames, serverWorldTimes, locale.get(GenericLang.UNKNOWN).toString());
    }

    public Pie serverPreferencePie(Map<String, Long> playtimeByServerName) {
        return new ServerPreferencePie(playtimeByServerName);
    }

    public WorldPie worldPie(WorldTimes worldTimes) {
        WorldAliasSettings worldAliasSettings = config.getWorldAliasSettings();
        Map<String, Long> playtimePerAlias = worldAliasSettings.getPlaytimePerAlias(worldTimes);
        Map<String, GMTimes> gmTimesPerAlias = worldAliasSettings.getGMTimesPerAlias(worldTimes);
        String[] colors = theme.getWorldPieColors();
        boolean orderByPercentage = config.isTrue(DisplaySettings.ORDER_WORLD_PIE_BY_PERCENTAGE);
        return new WorldPie(playtimePerAlias, gmTimesPerAlias, colors, orderByPercentage);
    }
}