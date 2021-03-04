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
package com.djrapitops.plan.extension;

import java.util.UUID;

/**
 * Interface for manually calling update methods on a registered {@link DataExtension}.
 * <p>
 * You can obtain an instance by registering an extension via {@link ExtensionService#register(DataExtension)}.
 * <p>
 * Plan calls the methods in DataExtension based on {@link CallEvents} defined by {@link }
 *
 * @author AuroraLS3
 */
public interface Caller {

    /**
     * Calls all player methods of the associated {@link DataExtension}.
     * <p>
     * Player methods have {@code UUID} or {@code String} as a method parameter and a Provider annotation.
     *
     * @param playerUUID UUID of the player.
     * @param playerName Name of the player.
     * @throws IllegalArgumentException If playerUUID or playerName is null.
     */
    void updatePlayerData(UUID playerUUID, String playerName) throws IllegalArgumentException;

    /**
     * Calls all server methods of the associated {@link DataExtension}.
     * <p>
     * Server methods have no parameters or {@link Group} method parameter and a Provider annotation.
     */
    void updateServerData();

}
