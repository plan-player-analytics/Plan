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
package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.system.SubSystem;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This Class contains the Cache.
 * <p>
 * Contains:
 * <ul>
 * <li>PlayerName cache, used for reducing database calls on chat events</li>
 * <li>DisplayName cache, used for reducing database calls on chat events</li>
 * </ul>
 *
 * @author Rsl1122
 */
@Singleton
public class DataCache extends SessionCache implements SubSystem {

    private final ErrorHandler errorHandler;

    private final Map<String, UUID> uuids;
    private final Map<UUID, String> displayNames;

    @Inject
    public DataCache(
            DBSystem dbSystem,
            ErrorHandler errorHandler
    ) {
        super(dbSystem);
        this.errorHandler = errorHandler;
        displayNames = new HashMap<>();
        uuids = new HashMap<>();
    }

    @Override
    public void enable() {
        // Nothing to enable
    }

    @Override
    public void disable() {
        uuids.clear();
        displayNames.clear();
    }

    /**
     * Used to update PlayerName and DisplayName caches.
     *
     * @param uuid        UUID of the player.
     * @param displayName DisplayName of the player.
     */
    public void updateDisplayName(UUID uuid, String displayName) {
        if (displayName != null) {
            displayNames.put(uuid, displayName);
        }
    }

    /**
     * Used to get the player display name in the cache.
     * <p>
     * If not cached, one from the database will be cached.
     *
     * @param uuid UUID of the player.
     * @return latest displayName or null if none are saved.
     */
    public String getDisplayName(UUID uuid) {
        String cached = displayNames.get(uuid);
        if (cached == null) {
            List<String> nicknames;
            try {
                nicknames = dbSystem.getDatabase().fetch().getNicknames(uuid);
                if (!nicknames.isEmpty()) {
                    return nicknames.get(nicknames.size() - 1);
                }
            } catch (DBOpException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        }
        return cached;
    }

    public UUID getUUIDof(String playerName) {
        return uuids.get(playerName);
    }
}
