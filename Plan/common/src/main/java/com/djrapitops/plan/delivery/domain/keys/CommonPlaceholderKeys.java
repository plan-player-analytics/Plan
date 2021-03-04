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
 * Similar to {@link CommonKeys}, but for {@link PlaceholderKey}s.
 *
 * @author AuroraLS3
 * @see PlaceholderKey for placeholder information
 */
class CommonPlaceholderKeys {

    static final PlaceholderKey<Integer> TIME_ZONE = new PlaceholderKey<>(Integer.class, "timeZone");
    static final PlaceholderKey<String> PLAYERS_GRAPH_COLOR = new PlaceholderKey<>(String.class, "playersGraphColor");
    static final PlaceholderKey<String> PLAYERS_ONLINE_SERIES = new PlaceholderKey<>(String.class, "playersOnlineSeries");
    static final PlaceholderKey<String> WORLD_MAP_HIGH_COLOR = new PlaceholderKey<>(String.class, "worldMapColHigh");
    static final PlaceholderKey<String> WORLD_MAP_LOW_COLOR = new PlaceholderKey<>(String.class, "worldMapColLow");
    static final PlaceholderKey<Integer> PLAYERS_ONLINE = new PlaceholderKey<>(Integer.class, "playersOnline");
    static final PlaceholderKey<Integer> PLAYERS_TOTAL = new PlaceholderKey<>(Integer.class, "playersTotal");
    static final PlaceholderKey<String> WORLD_MAP_SERIES = new PlaceholderKey<>(String.class, "geoMapSeries");
    static final PlaceholderKey<String> ACTIVITY_STACK_SERIES = new PlaceholderKey<>(String.class, "activityStackSeries");
    static final PlaceholderKey<String> ACTIVITY_STACK_CATEGORIES = new PlaceholderKey<>(String.class, "activityStackCategories");
    static final PlaceholderKey<String> ACTIVITY_PIE_SERIES = new PlaceholderKey<>(String.class, "activityPieSeries");
    static final PlaceholderKey<String> COUNTRY_CATEGORIES = new PlaceholderKey<>(String.class, "countryCategories");
    static final PlaceholderKey<String> COUNTRY_SERIES = new PlaceholderKey<>(String.class, "countrySeries");

    static final PlaceholderKey<String> HEALTH_NOTES = new PlaceholderKey<>(String.class, "healthNotes");
    static final PlaceholderKey<Double> HEALTH_INDEX = new PlaceholderKey<>(Double.class, "healthIndex");

    static final PlaceholderKey<Integer> PLAYERS_DAY = new PlaceholderKey<>(Integer.class, "playersDay");
    static final PlaceholderKey<Integer> PLAYERS_WEEK = new PlaceholderKey<>(Integer.class, "playersWeek");
    static final PlaceholderKey<Integer> PLAYERS_MONTH = new PlaceholderKey<>(Integer.class, "playersMonth");
    static final PlaceholderKey<Integer> PLAYERS_NEW_DAY = new PlaceholderKey<>(Integer.class, "playersNewDay");
    static final PlaceholderKey<Integer> PLAYERS_NEW_WEEK = new PlaceholderKey<>(Integer.class, "playersNewWeek");
    static final PlaceholderKey<Integer> PLAYERS_NEW_MONTH = new PlaceholderKey<>(Integer.class, "playersNewMonth");

    static final PlaceholderKey<String> REFRESH_TIME_F = new PlaceholderKey<>(String.class, "refresh");
    static final PlaceholderKey<String> REFRESH_TIME_FULL_F = new PlaceholderKey<>(String.class, "refreshFull");
    static final PlaceholderKey<String> LAST_PEAK_TIME_F = new PlaceholderKey<>(String.class, "lastPeakTime");
    static final PlaceholderKey<String> ALL_TIME_PEAK_TIME_F = new PlaceholderKey<>(String.class, "bestPeakTime");
    static final PlaceholderKey<String> PLAYERS_LAST_PEAK = new PlaceholderKey<>(String.class, "playersLastPeak");
    static final PlaceholderKey<String> PLAYERS_ALL_TIME_PEAK = new PlaceholderKey<>(String.class, "playersBestPeak");

    private CommonPlaceholderKeys() {
        /* static variable class */
    }

}