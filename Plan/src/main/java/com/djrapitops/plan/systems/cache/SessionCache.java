package com.djrapitops.plan.systems.cache;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.systems.processing.Processor;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plugin.api.utility.log.Log;

import java.sql.SQLException;
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
    protected final Plan plugin;

    /**
     * Class Constructor.
     */
    public SessionCache(Plan plugin) {
        this.plugin = plugin;
    }

    public void cacheSession(UUID uuid, Session session) {
        activeSessions.put(uuid, session);
        plugin.addToProcessQueue(new Processor<Plan>(plugin) {
            @Override
            public void process() {
                plugin.getInfoManager().cachePlayer(uuid);
            }
        });
    }

    public void endSession(UUID uuid, long time) {
        try {
            Session session = activeSessions.get(uuid);
            if (session == null) {
                return;
            }
            session.endSession(time);
            plugin.getDB().getSessionsTable().saveSession(uuid, session);
        } catch (SQLException e) {
            Log.toLog(this.getClass().getName(), e);
        } finally {
            activeSessions.remove(uuid);
            plugin.getInfoManager().cachePlayer(uuid);
        }
    }

    public void refreshActiveSessionsState() {
        for (Session session : activeSessions.values()) {
            session.getWorldTimes().updateState(MiscUtils.getTime());
        }
    }

    /**
     * Used to get the Session of the player in the sessionCache.
     *
     * @param uuid UUID of the player.
     * @return Session or null if not cached.
     */
    public Optional<Session> getCachedSession(UUID uuid) {
        Session session = activeSessions.get(uuid);
        if (session != null) {
            return Optional.of(session);
        }
        return Optional.empty();
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
}
