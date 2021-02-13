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

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.gathering.domain.Session;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to store active sessions of players in memory.
 *
 * @author AuroraLS3
 */
@Singleton
public class SessionCache {

    private static final Map<UUID, Session> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    @Inject
    public SessionCache() {
        // Dagger requires empty inject constructor
    }

    public static Map<UUID, Session> getActiveSessions() {
        refreshActiveSessionsState();
        return Collections.unmodifiableMap(new HashMap<>(ACTIVE_SESSIONS));
    }

    public static void clear() {
        ACTIVE_SESSIONS.clear();
    }

    public static void refreshActiveSessionsState() {
        ACTIVE_SESSIONS.values().forEach(Session::updateState);
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param playerUUID UUID of the player.
     * @return Optional with the session inside it if found.
     */
    public static Optional<Session> getCachedSession(UUID playerUUID) {
        Optional<Session> found = Optional.ofNullable(ACTIVE_SESSIONS.get(playerUUID));
        found.ifPresent(Session::updateState);
        return found;
    }

    /**
     * Cache a new session.
     *
     * @param playerUUID UUID of the player
     * @param session    Session to cache.
     * @return Optional: previous session. Recipients of this object should decide if it needs to be saved.
     */
    public Optional<Session> cacheSession(UUID playerUUID, Session session) {
        Optional<Session> inProgress = Optional.empty();
        if (getCachedSession(playerUUID).isPresent()) {
            inProgress = endSession(playerUUID, session.getUnsafe(SessionKeys.START));
        }
        ACTIVE_SESSIONS.put(playerUUID, session);
        return inProgress;
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
        ACTIVE_SESSIONS.remove(playerUUID);
        session.endSession(time);
        return Optional.of(session);
    }
}
