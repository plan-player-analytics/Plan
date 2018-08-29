package com.djrapitops.plan.system.cache;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.system.database.databases.Database;
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

    private final Database database;

    public SessionCache(Database database) {
        this.database = database;
    }

    @Deprecated
    public static SessionCache getInstance() {
        SessionCache dataCache = CacheSystem.getInstance().getDataCache();
        Verify.nullCheck(dataCache, () -> new IllegalStateException("Data Cache was not initialized."));
        return dataCache;
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
    public void endSession(UUID uuid, long time) {
        Session session = activeSessions.get(uuid);
        if (session == null) {
            return;
        }
        if (session.getUnsafe(SessionKeys.START) > time) {
            return;
        }
        try {
            session.endSession(time);
            // Might throw a DBOpException
            database.save().session(uuid, session);
        } finally {
            removeSessionFromCache(uuid);
        }
    }

    protected void removeSessionFromCache(UUID uuid) {
        activeSessions.remove(uuid);
    }
}
