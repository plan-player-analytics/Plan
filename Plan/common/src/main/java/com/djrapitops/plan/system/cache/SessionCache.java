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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store active sessions of players in memory.
 *
 * @author Rsl1122
 */
@Singleton
public class SessionCache {

    private static final Map<UUID, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    @Inject
    public SessionCache() {
        // Dagger requires empty inject constructor
    }

    public static Map<UUID, Session> getActiveSessions() {
        return Collections.unmodifiableMap(ACTIVE_SESSIONS);
    }

    public static void clear() {
        ACTIVE_SESSIONS.clear();
    }

    public static void refreshActiveSessionsState() {
        for (Session session : ACTIVE_SESSIONS.values()) {
            session.getUnsafe(SessionKeys.WORLD_TIMES).updateState(System.currentTimeMillis());
        }
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param playerUUID UUID of the player.
     * @return Optional with the session inside it if found.
     */
    public static Optional<Session> getCachedSession(UUID playerUUID) {
        return Optional.ofNullable(ACTIVE_SESSIONS.get(playerUUID));
    }

    /**
     * Cache a new session.
     *
     * @param playerUUID UUID of the player
     * @param session    Session to cache.
     * @return Optional: previous session. Recipients of this object should decide if it needs to be saved.
     */
    public Optional<Session> cacheSession(UUID playerUUID, Session session) {
        if (getCachedSession(playerUUID).isPresent()) {
            return endSession(playerUUID, session.getUnsafe(SessionKeys.START));
        }
        ACTIVE_SESSIONS.put(playerUUID, session);
        return Optional.empty();
    }

    /**
     * End a session and save it to database.
     *
     * @param playerUUID UUID of the player.
     * @param time       Time the session ended.
     * @return Optional: ended session. Recipients of this object should decide if it needs to be saved.
     */
    public Optional<Session> endSession(UUID playerUUID, long time) {
        Session session = ACTIVE_SESSIONS.get(playerUUID);
        if (session == null || session.getUnsafe(SessionKeys.START) > time) {
            return Optional.empty();
        }
        removeSessionFromCache(playerUUID);
        session.endSession(time);
        return Optional.of(session);
    }

    protected void removeSessionFromCache(UUID playerUUID) {
        ACTIVE_SESSIONS.remove(playerUUID);
    }
}
