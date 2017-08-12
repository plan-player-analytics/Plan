package main.java.com.djrapitops.plan.data.cache;

import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

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

    private final HashMap<UUID, SessionData> activeSessions;

    /**
     * Class Constructor.
     */
    public SessionCache() {
        this.activeSessions = new HashMap<>();
    }

    /**
     * Starts a session for a player at the current moment.
     *
     * @param uuid UUID of the player.
     */
    public void startSession(UUID uuid) {
        SessionData session = new SessionData(MiscUtils.getTime());
        activeSessions.put(uuid, session);
    }

    /**
     * Ends a session for a player at the current moment.
     *
     * @param uuid UUID of the player.
     */
    public void endSession(UUID uuid) {
        SessionData currentSession = activeSessions.get(uuid);
        if (currentSession != null) {
            currentSession.endSession(MiscUtils.getTime());
        }
    }

    /**
     * Used to get the SessionData of the player in the sessionCache.
     *
     * @param uuid UUID of the player.
     * @return SessionData or null if not cached.
     */
    public SessionData getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }

    /**
     * Add a session to the UserData object if it is cached and has been ended.
     *
     * @param data UserData object a session should be added to.
     */
    public void addSession(UserData data) {
        UUID uuid = data.getUuid();
        SessionData currentSession = activeSessions.get(uuid);
        if (currentSession != null && currentSession.isValid() && !data.getSessions().contains(currentSession)) {
            data.addSession(currentSession);
            activeSessions.remove(uuid);
        }
    }

    /**
     * Used to get the Map of active sessions.
     * <p>
     * Used for testing.
     *
     * @return key:value UUID:SessionData
     */
    public Map<UUID, SessionData> getActiveSessions() {
        return activeSessions;
    }
}
