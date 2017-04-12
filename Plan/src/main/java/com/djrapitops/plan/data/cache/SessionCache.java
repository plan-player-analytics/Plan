
package main.java.com.djrapitops.plan.data.cache;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.cache.DataCacheHandler;

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
    
    public void endSession(UUID uuid) {
        SessionData currentSession = activeSessions.get(uuid);
        if (currentSession != null) {
            long now = new Date().toInstant().getEpochSecond() * (long) 1000;
            currentSession.endSession(now);
        }
    }
    
    /**
     *
     * @param data
     */
    public void addSession(UserData data) {
        UUID uuid = data.getUuid();
        SessionData currentSession = activeSessions.get(uuid);
        if (currentSession != null && currentSession.isValid()) {
            data.addSession(currentSession);
            activeSessions.remove(uuid);
        }
    }

    public HashMap<UUID, SessionData> getActiveSessions() {
        return activeSessions;
    }
}
