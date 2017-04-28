
package main.java.com.djrapitops.plan.data.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;

/**
 *
 * @author Rsl1122
 */
public class SessionCache {
    private final HashMap<UUID, SessionData> activeSessions;

    /**
     *
     */
    public SessionCache() {
        this.activeSessions = new HashMap<>();    
    }
    
    /**
     *
     * @param uuid
     */
    public void startSession(UUID uuid) {
        long now = new Date().toInstant().getEpochSecond() * (long) 1000;
        SessionData session = new SessionData(now);
        activeSessions.put(uuid, session);
    }
    
    /**
     *
     * @param uuid
     */
    public void endSession(UUID uuid) {
        SessionData currentSession = activeSessions.get(uuid);
        if (currentSession != null) {
            long now = new Date().toInstant().getEpochSecond() * (long) 1000;
            currentSession.endSession(now);
        }
    }
    
    /**
     *
     * @param uuid
     * @return
     */
    public SessionData getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }
    
    /**
     *
     * @param data
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
     *
     * @return
     */
    public HashMap<UUID, SessionData> getActiveSessions() {
        return activeSessions;
    }
}
