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

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.system.database.DBSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is used to store active sessions of players in memory.
 *
 * @author Rsl1122
 */
public class SessionCache {

    private static final Map<UUID, Session> activeSessions = new HashMap<>();

    protected final DBSystem dbSystem;

    public SessionCache(DBSystem dbSystem) {
        this.dbSystem = dbSystem;
    }

    public static Map<UUID, Session> getActiveSessions() {
        return activeSessions;
    }

    public static void clear() {
        activeSessions.clear();
    }

    public static void refreshActiveSessionsState() {
        for (Session session : activeSessions.values()) {
            session.getUnsafe(SessionKeys.WORLD_TIMES).updateState(System.currentTimeMillis());
        }
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param uuid UUID of the player.
     * @return Optional with the session inside it if found.
     */
    public static Optional<Session> getCachedSession(UUID uuid) {
        return Optional.ofNullable(activeSessions.get(uuid));
    }

    public void cacheSession(UUID uuid, Session session) {
        if (getCachedSession(uuid).isPresent()) {
            endSession(uuid, System.currentTimeMillis());
        }
        activeSessions.put(uuid, session);
    }

    /**
     * End a session and save it to database.
     *
     * @param uuid UUID of the player.
     * @param time Time the session ended.
     * @throws com.djrapitops.plan.api.exceptions.database.DBOpException If saving failed.
     */
    public Optional<Session> endSession(UUID uuid, long time) {
        Session session = activeSessions.get(uuid);
        if (session == null || session.getUnsafe(SessionKeys.START) > time) {
            return Optional.empty();
        }
        try {
            session.endSession(time);
            // Might throw a DBOpException
            // TODO Refactor to use Event transactions when available.
            dbSystem.getDatabase().save().session(uuid, session);
            return Optional.of(session);
        } finally {
            removeSessionFromCache(uuid);
        }
    }

    protected void removeSessionFromCache(UUID uuid) {
        activeSessions.remove(uuid);
    }
}
