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
package com.djrapitops.plan.delivery.domain.keys;

/**
 * Key objects used for Analysis.
 * <p>
 * PlaceholderKey objects can be used for directly replacing a value on the html.
 *
 * @author AuroraLS3
 * @see com.djrapitops.plan.data.store.containers.AnalysisContainer for Suppliers for each Key.
 * @deprecated AnalysisContainer can no longer be obtained, so this is deprecated.
 */
@Deprecated
public class AnalysisKeys {

    public static final PlaceholderKey<Integer> TIME_ZONE = CommonPlaceholderKeys.TIME_ZONE;
    public static final PlaceholderKey<Integer> FIRST_DAY = new PlaceholderKey<>(Integer.class, "firstDay");
    public static final PlaceholderKey<Integer> TPS_MEDIUM = new PlaceholderKey<>(Integer.class, "tpsMedium");
    public static final PlaceholderKey<Integer> TPS_HIGH = new PlaceholderKey<>(Integer.class, "tpsHigh");
    public static final PlaceholderKey<Integer> DISK_MEDIUM = new PlaceholderKey<>(Integer.class, "diskMedium");
    public static final PlaceholderKey<Integer> DISK_HIGH = new PlaceholderKey<>(Integer.class, "diskHigh");
    public static final PlaceholderKey<Integer> PLAYERS_MAX = new PlaceholderKey<>(Integer.class, "playersMax");
    public static final PlaceholderKey<Integer> PLAYERS_ONLINE = CommonPlaceholderKeys.PLAYERS_ONLINE;
    public static final PlaceholderKey<Integer> PLAYERS_TOTAL = CommonPlaceholderKeys.PLAYERS_TOTAL;
    //
    public static final PlaceholderKey<String> WORLD_PIE_COLORS = new PlaceholderKey<>(String.class, "worldPieColors");
    public static final PlaceholderKey<String> GM_PIE_COLORS = new PlaceholderKey<>(String.class, "gmPieColors");
    public static final PlaceholderKey<String> ACTIVITY_PIE_COLORS = new PlaceholderKey<>(String.class, "activityPieColors");
    public static final PlaceholderKey<String> PLAYERS_GRAPH_COLOR = CommonPlaceholderKeys.PLAYERS_GRAPH_COLOR;
    public static final PlaceholderKey<String> TPS_HIGH_COLOR = new PlaceholderKey<>(String.class, "tpsHighColor");
    public static final PlaceholderKey<String> TPS_MEDIUM_COLOR = new PlaceholderKey<>(String.class, "tpsMediumColor");
    public static final PlaceholderKey<String> TPS_LOW_COLOR = new PlaceholderKey<>(String.class, "tpsLowColor");
    public static final PlaceholderKey<String> AVG_PING_COLOR = new PlaceholderKey<>(String.class, "avgPingColor");
    public static final PlaceholderKey<String> MIN_PING_COLOR = new PlaceholderKey<>(String.class, "minPingColor");
    public static final PlaceholderKey<String> MAX_PING_COLOR = new PlaceholderKey<>(String.class, "maxPingColor");
    public static final PlaceholderKey<String> WORLD_MAP_HIGH_COLOR = CommonPlaceholderKeys.WORLD_MAP_HIGH_COLOR;
    public static final PlaceholderKey<String> WORLD_MAP_LOW_COLOR = CommonPlaceholderKeys.WORLD_MAP_LOW_COLOR;

    private AnalysisKeys() {
        /* Static variable class */
    }
}