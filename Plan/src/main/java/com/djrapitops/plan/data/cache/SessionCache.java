package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.SessionData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This class is used to store active sessions of players in memory.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class SessionCache {

    private static final Map<UUID, SessionData> activeSessions = new HashMap<>();

    /**
     * Class Constructor.
     */
    public SessionCache() {
    }

    public void cacheSession(UUID uuid, SessionData session) {
        activeSessions.put(uuid, session);
    }

    public void endSession(UUID uuid, long time) {
        SessionData session = activeSessions.get(uuid);
        if (session == null) {
            return;
        }
        session.endSession(time);

    }

    /**
     * Starts a session for a player at the current moment.
     *
     * @param uuid UUID of the player.
     */
    @Deprecated
    public void startSession(UUID uuid) {
    }

    /**
     * Ends a session for a player at the current moment.
     *
     * @param uuid UUID of the player.
     */
    @Deprecated
    public void endSession(UUID uuid) {
    }

    /**
     * Used to get the SessionData of the player in the sessionCache.
     *
     * @param uuid UUID of the player.
     * @return SessionData or null if not cached.
     */
    @Deprecated
    public SessionData getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    /**
     * Used to get the Map of active sessions.
     * <p>
     * Used for testing.
     *
     * @return key:value UUID:SessionData
     */
    @Deprecated
    public Map<UUID, SessionData> getActiveSessions() {
        return activeSessions;
    }
}
