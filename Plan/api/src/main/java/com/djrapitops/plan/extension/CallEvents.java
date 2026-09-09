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

/**
 * Enum representing different events when Plan calls methods of {@link DataExtension} automatically.
 * <p>
 * You can also call the update methods via {@link Caller} manually.
 *
 * @author AuroraLS3
 */
public enum CallEvents {

    /**
     * This event represents a manual call via {@link Caller}.
     * Definition inside {@link DataExtension#callExtensionMethodsOn()} is NOT REQUIRED for using Caller methods.
     */
    MANUAL,
    /**
     * This event represents call to player methods on a Player Join event.
     * <p>
     * The call is made from a listener at the last event priority (Bukkit/Bungee: MONITOR, Sponge: POST).
     * Method calls are asynchronous.
     */
    PLAYER_JOIN,
    /**
     * This event represents call to player methods while player is online.
     * <p>
     * The call is made periodically from an async task.
     */
    PLAYER_PERIODICAL,
    /**
     * This event represents a call to player methods on a Player Leave event.
     * <p>
     * The call is made from a listener at the first event priority (Bukkit/Bungee: LOWEST, Sponge: PRE).
     * Method calls are asynchronous.
     */
    PLAYER_LEAVE,
    /**
     * This event represents a call to server methods when the {@link DataExtension} is registered.
     * <p>
     * Server methods include any {@link Group} parameter methods.
     * <p>
     * Method calls are asynchronous.
     */
    SERVER_EXTENSION_REGISTER,
    /**
     * This event represents a call to server methods via a periodical task.
     * <p>
     * Server methods include any {@link Group} parameter methods.
     * <p>
     * Periodic task with a runs user configured period (Plan config).
     * Method calls are asynchronous.
     */
    SERVER_PERIODICAL

}
