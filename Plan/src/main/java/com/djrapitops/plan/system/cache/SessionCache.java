package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.PlanSystem;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * This class is used to store active sessions of players in memory.
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public class SessionCache {

    private static final Map<UUID, Session> activeSessions = new HashMap<>();
    protected final PlanSystem system;

    public SessionCache(PlanSystem system) {
        this.system = system;
    }

    public static SessionCache getInstance() {
        SessionCache dataCache = CacheSystem.getInstance().getDataCache();
        Verify.nullCheck(dataCache, () -> new IllegalStateException("Data Cache was not initialized."));
        return dataCache;
    }

    /**
     * Used to get the Map of active sessions.
     * <p>
     * Used for testing.
     *
     * @return key:value UUID:Session
     */
    public static Map<UUID, Session> getActiveSessions() {
        return activeSessions;
    }

    public static void clear() {
        activeSessions.clear();
    }

    public void cacheSession(UUID uuid, Session session) {
        activeSessions.put(uuid, session);
    }

    public void endSession(UUID uuid, long time) {
        try {
            Session session = activeSessions.get(uuid);
            if (session == null) {
                return;
            }
            session.endSession(time);
            Database.getActive().save().session(uuid, session);
        } catch (DBOpException e) {
            Log.toLog(this.getClass(), e);
        } finally {
            activeSessions.remove(uuid);
        }
    }

    public static void refreshActiveSessionsState() {
        for (Session session : activeSessions.values()) {
            session.getWorldTimes().updateState(System.currentTimeMillis());
        }
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param uuid UUID of the player.
     * @return Session or null if not cached.
     */
    public static Optional<Session> getCachedSession(UUID uuid) {
        Session session = activeSessions.get(uuid);
        if (session != null) {
            return Optional.of(session);
        }
        return Optional.empty();
    }

}
