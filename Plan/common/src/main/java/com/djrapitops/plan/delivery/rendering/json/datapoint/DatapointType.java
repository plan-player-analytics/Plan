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

/**
 * Types of {@link Datapoint datapoints}.
 *
 * @author AuroraLS3
 */
public enum DatapointType {
    PLAYTIME(Playtime.class),
    WORLD_PIE(WorldPie.class),
    AFK_TIME(AfkTime.class),
    AFK_TIME_PERCENTAGE(AfkTimePercentage.class),
    SERVER_OCCUPIED(ServerOccupied.class),
    MOST_ACTIVE_GAME_MODE(MostActiveGameMode.class),
    MOST_ACTIVE_WORLD(MostActiveWorld.class),
    SERVER_PIE(ServerPie.class);

    private final Class<? extends Datapoint<?>> datapointClass;

    DatapointType(Class<? extends Datapoint<?>> datapointClass) {
        this.datapointClass = datapointClass;
    }

    public static DatapointType find(String name) {
        try {
            return DatapointType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Unknown type");
        }
    }

    public Class<? extends Datapoint<?>> getDatapointClass() {
        return datapointClass;
    }
}
