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
package com.djrapitops.plan.identification;

import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.UserIdentifierQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.api.utility.UUIDFetcher;
import com.djrapitops.plugin.logging.L;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.UUID;

/**
 * Utility for fetching a user's UUID.
 * <p>
 * Attempts are made in order:
 * - Parse UUID out of the given String
 * - Find an UUID from the database matching the player name
 * - Find an UUID from Mojang API that matches the player name
 *
 * @author Rsl1122
 */
@Singleton
public class UUIDUtility {

    private final DBSystem dbSystem;
    private final ErrorLogger errorLogger;

    @Inject
    public UUIDUtility(DBSystem dbSystem, ErrorLogger errorLogger) {
        this.dbSystem = dbSystem;
        this.errorLogger = errorLogger;
    }

    public static Optional<UUID> parseFromString(String uuidString) {
        try {
            return Optional.of(UUID.fromString(uuidString));
        } catch (IllegalArgumentException malformedUUIDException) {
            return Optional.empty();
        }
    }

    /**
     * Get UUID of a player.
     *
     * @param playerName Player's name
     * @return UUID of the player
     */
    public UUID getUUIDOf(String playerName) {
        if (playerName == null) throw new IllegalArgumentException("Player name can not be null!");
        UUID uuid = getUUIDFromString(playerName);
        if (uuid != null) return uuid;

        return getUUIDFromDB(playerName)
                .orElse(getUUIDViaUUIDFetcher(playerName));
    }

    private UUID getUUIDFromString(String playerName) {
        try {
            return UUID.fromString(playerName);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
    }

    private UUID getUUIDViaUUIDFetcher(String playerName) {
        try {
            return UUIDFetcher.getUUIDOf(playerName);
        } catch (Exception | NoClassDefFoundError ignored) {
            return null;
        }
    }

    private Optional<UUID> getUUIDFromDB(String playerName) {
        try {
            return dbSystem.getDatabase().query(UserIdentifierQueries.fetchPlayerUUIDOf(playerName));
        } catch (DBOpException e) {
            errorLogger.log(L.ERROR, e);
            return Optional.empty();
        }
    }
}
