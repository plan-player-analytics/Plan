package main.java.com.djrapitops.plan.data.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import main.java.com.djrapitops.plan.Log;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

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
        long now = MiscUtils.getTime();
        Log.debug(uuid + ": Starting a session: " + now);
        SessionData session = new SessionData(now);
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
            long now = MiscUtils.getTime();
            Log.debug(uuid + ": Ending a session: " + now);
            currentSession.endSession(now);
        }
    }

    /**
     * Used to get the SessionData of the player in the sessionCache.
     *
     * @param uuid UUId of the player.
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
        Log.debug("Adding a session: " + uuid + " " + currentSession);
        if (currentSession != null && currentSession.isValid() && !data.getSessions().contains(currentSession)) {
            data.addSession(currentSession);
            activeSessions.remove(uuid);
        }
    }

    /**
     * Used to get the Map of active sessions.
     *
     * Used for testing.
     * 
     * @return key:value UUID:SessionData
     */
    public Map<UUID, SessionData> getActiveSessions() {
        return activeSessions;
    }
}
