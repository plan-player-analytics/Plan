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

import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.gathering.domain.event.PlayerLeave;

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

    private static final Map<UUID, ActiveSession> ACTIVE_SESSIONS = new ConcurrentHashMap<>();

    @Inject
    public SessionCache() {
        // Dagger requires empty inject constructor
    }

    public static Collection<ActiveSession> getActiveSessions() {
        refreshActiveSessionsState();
        return new HashSet<>(ACTIVE_SESSIONS.values());
    }

    public static void clear() {
        ACTIVE_SESSIONS.clear();
    }

    public static void refreshActiveSessionsState() {
        ACTIVE_SESSIONS.values().forEach(ActiveSession::updateState);
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param playerUUID UUID of the player.
     * @return Optional with the session inside it if found.
     */
    public static Optional<ActiveSession> getCachedSession(UUID playerUUID) {
        Optional<ActiveSession> found = Optional.ofNullable(ACTIVE_SESSIONS.get(playerUUID));
        found.ifPresent(ActiveSession::updateState);
        return found;
    }

    /**
     * Cache a new session.
     *
     * @param playerUUID UUID of the player
     * @param newSession Session to cache.
     * @return Optional: previous session. Recipients of this object should decide if it needs to be saved.
     */
    public Optional<FinishedSession> cacheSession(UUID playerUUID, ActiveSession newSession) {
        Optional<ActiveSession> inProgress = getCachedSession(playerUUID);
        Optional<FinishedSession> finished = Optional.empty();
        if (inProgress.isPresent()) {
            finished = endSession(playerUUID, newSession.getStart(), inProgress.get());
        }
        ACTIVE_SESSIONS.put(playerUUID, newSession);
        return finished;
    }

    /**
     * End a session and save it to database.
     *
     * @param playerUUID    UUID of the player.
     * @param time          Time the session ended.
     * @param activeSession Currently active session
     * @return Optional: ended session. Recipients of this object should decide if it needs to be saved.
     */
    public Optional<FinishedSession> endSession(UUID playerUUID, long time, ActiveSession activeSession) {
        if (activeSession == null || activeSession.getStart() > time) {
            return Optional.empty();
        }
        ACTIVE_SESSIONS.remove(playerUUID);
        return Optional.of(activeSession.toFinishedSession(time));
    }

    public Optional<FinishedSession> endSession(UUID playerUUID, PlayerLeave leave) {
        return endSession(playerUUID, leave.getTime());
    }

    public Optional<FinishedSession> endSession(UUID playerUUID, long time) {
        return endSession(playerUUID, time, ACTIVE_SESSIONS.get(playerUUID));
    }
}
