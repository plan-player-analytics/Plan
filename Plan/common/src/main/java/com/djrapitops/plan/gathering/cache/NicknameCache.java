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
package com.djrapitops.plan.gathering.cache;

import com.djrapitops.plan.SubSystem;
import com.djrapitops.plan.delivery.domain.Nickname;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.NicknameQueries;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Used for caching nicknames when the player is online.
 *
 * @author AuroraLS3
 */
@Singleton
public class NicknameCache implements SubSystem {

    private final DBSystem dbSystem;
    private final ServerInfo serverInfo;
    private final ErrorLogger errorLogger;

    private final Map<UUID, String> displayNames;

    @Inject
    public NicknameCache(
            DBSystem dbSystem,
            ServerInfo serverInfo,
            ErrorLogger errorLogger
    ) {
        this.dbSystem = dbSystem;
        this.serverInfo = serverInfo;
        this.errorLogger = errorLogger;
        displayNames = new HashMap<>();
    }

    @Override
    public void enable() {
        // Nothing to enable
    }

    @Override
    public void disable() {
        displayNames.clear();
    }

    public void removeDisplayName(UUID uuid) {
        displayNames.remove(uuid);
    }

    /**
     * Used to get the player display name in the cache.
     * <p>
     * If not cached, one from the database will be cached.
     *
     * @param uuid UUID of the player.
     * @return latest displayName or null if none are saved.
     */
    public Optional<String> getDisplayName(UUID uuid) {
        String cached = displayNames.get(uuid);

        if (cached == null) {
            Optional<String> found = getFromDatabase(uuid);
            if (found.isPresent()) {
                displayNames.put(uuid, found.get());
                return found;
            }
        }
        return Optional.empty();
    }

    private Optional<String> getFromDatabase(UUID uuid) {
        try {
            return dbSystem.getDatabase().query(
                    NicknameQueries.fetchLastSeenNicknameOfPlayer(uuid, serverInfo.getServerUUID())
            ).map(Nickname::getName);
        } catch (DBOpException e) {
            errorLogger.error(e);
        }
        return Optional.empty();
    }
}
