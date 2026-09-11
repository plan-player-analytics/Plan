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
package com.djrapitops.plan.extension.graph;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Return value for Player graph from {@link com.djrapitops.plan.extension.annotation.GraphProvider} annotated method.
 *
 * @author AuroraLS3
 */
public abstract class PlayerGraphDataSource {
    /**
     * Implement this method to return newest datapoint.
     * <p>
     * {@link DataPoint} should return value for X axis and all values for Y axis.
     *
     * @param currentTimestamp Epoch millisecond of method call time.
     * @param playerUUID       UUID of the player.
     * @param playerName       Name of the player.
     * @return Optional.empty() if there's no new point to add.
     */
    public abstract Optional<DataPoint> getPoint(long currentTimestamp, UUID playerUUID, String playerName);

    /**
     * Override this method to return point history for your data.
     *
     * @param currentTimestamp Epoch millisecond of method call time.
     * @param playerUUID       UUID of the player.
     * @param playerName       Name of the player.
     * @return List of points to add to Plan, each {@link DataPoint} including value for X axis and all values for Y axis.
     */
    public List<DataPoint> getPointHistory(long currentTimestamp, UUID playerUUID, String playerName) {
        return Collections.emptyList();
    }

    /**
     * Implement this method to return {@link SeriesMetadata} for each Y axis value.
     * <p>
     * This method will be called when Extension is registered, and any time DataPoint has more values than previously.
     * <p>
     * Index in List defines which series metadata is for - if series stops getting data return null in that index.
     *
     * @return {@code List.of({metadata for Y series of DataPoint#values[0]}, {metadata for Y series of DataPoint#values[1]}, ...etc)}
     */
    public abstract List<SeriesMetadata> getSeriesMetadata();
}
