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
package com.djrapitops.plan.delivery.rendering.json.datapoint;

import com.djrapitops.plan.delivery.rendering.json.datapoint.types.*;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Types of {@link Datapoint datapoints}.
 *
 * @author AuroraLS3
 */
public enum DatapointType {
    PLAYTIME(Playtime.class, DatapointCacheKey.SESSION),
    WORLD_PIE(WorldPie.class, DatapointCacheKey.SESSION),
    AFK_TIME(AfkTime.class, DatapointCacheKey.SESSION),
    AFK_TIME_PERCENTAGE(AfkTimePercentage.class, DatapointCacheKey.SESSION),
    SERVER_OCCUPIED(ServerOccupied.class, DatapointCacheKey.SESSION, DatapointCacheKey.TPS),
    MOST_ACTIVE_GAME_MODE(MostActiveGameMode.class, DatapointCacheKey.SESSION),
    MOST_ACTIVE_WORLD(MostActiveWorld.class, DatapointCacheKey.SESSION),
    SERVER_PIE(ServerPie.class, DatapointCacheKey.SESSION);

    private final Class<? extends Datapoint<?>> datapointClass;
    private final DatapointCacheKey[] cacheKeys;

    DatapointType(Class<? extends Datapoint<?>> datapointClass, DatapointCacheKey... cacheKeys) {
        this.datapointClass = datapointClass;
        this.cacheKeys = cacheKeys;
    }

    public static DatapointType find(String name) {
        try {
            return DatapointType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown type");
        }
    }

    @SuppressWarnings("unused")
    public Class<? extends Datapoint<?>> getDatapointClass() {
        return datapointClass;
    }

    public Set<DatapointCacheKey> getCacheKeys() {
        return new HashSet<>(Arrays.asList(cacheKeys));
    }
}
