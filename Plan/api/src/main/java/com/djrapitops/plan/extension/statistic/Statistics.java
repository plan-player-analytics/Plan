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
package com.djrapitops.plan.extension.statistic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Return value for {@link com.djrapitops.plan.extension.annotation.StatisticsProvider}.
 * <p>
 * Stores key-number statistical data similar to Minecraft statistics.
 * <p>
 * Statistics can be grouped together by placing a : in the key, for example group:key1, group:key2
 * <p>
 * Player statistics will be automatically summed up into server-wide statistics.
 * <p>
 * Requires Capability DATA_EXTENSION_STATISTICS_API.
 *
 * @author AuroraLS3
 */
public final class Statistics {

    private final Map<String, Long> valueByKey;

    public Statistics() {
        this.valueByKey = new HashMap<>();
    }

    public static Statistics create() {
        return new Statistics();
    }

    /**
     * Place statistic in the object.
     *
     * @param key   String, with groups separated by :, e.g. "group_level_1:group_level_2:key"
     * @param value Statistical value.
     * @return This object.
     */
    public Statistics put(String key, Long value) {
        valueByKey.put(key, value);
        return this;
    }

    /**
     * Place statistics in the object.
     *
     * @param statistics String-Long map, with groups separated by :, e.g. "group_level_1:group_level_2:key"
     * @return This object.
     * @throws IllegalArgumentException if map contains null keys or values.
     */
    public Statistics putAll(Map<String, Long> statistics) {
        this.valueByKey.putAll(statistics);
        return this;
    }

    /**
     * The method Plan uses to get access to the data.
     *
     * @return Values stored in this object.
     */
    public Map<String, Long> getValueByKey() {
        return valueByKey;
    }

    @SuppressWarnings({"java:S4034", "SimplifyStreamApiCallChains"}) // anyMatch is faster operation
    public boolean isEmpty() {
        return !valueByKey.values().stream().anyMatch(Objects::nonNull);
    }
}
