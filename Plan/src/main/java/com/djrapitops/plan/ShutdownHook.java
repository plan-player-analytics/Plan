/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.database.DBException;
import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Thread that is run when JVM shuts down.
 * <p>
 * Saves active sessions to the Database (PlayerQuitEvent is not called)
 *
 * @author Rsl1122
 */
public class ShutdownHook extends Thread {

    private static ShutdownHook activated;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public ShutdownHook(Database database, ErrorHandler errorHandler) {
        this.database = database;
        this.errorHandler = errorHandler;
    }

    private static boolean isActivated() {
        return activated != null;
    }

    private static void activate(ShutdownHook hook) {
        activated = hook;
        Runtime.getRuntime().addShutdownHook(hook);
    }

    private static void deactivate() {
        Runtime.getRuntime().removeShutdownHook(activated);
        activated = null;
    }

    public void register() {
        if (isActivated()) {
            deactivate();
        }
        activate(this);
    }

    @Override
    public void run() {
        try {
            Map<UUID, Session> activeSessions = SessionCache.getActiveSessions();
            long now = System.currentTimeMillis();
            saveActiveSessions(activeSessions, now);
        } catch (IllegalStateException ignored) {
            /* Database is not initialized */
        } catch (DBInitException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        } finally {
            try {
                database.close();
            } catch (DBException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        }
    }

    private void saveActiveSessions(Map<UUID, Session> activeSessions, long now) throws DBInitException {
        for (Map.Entry<UUID, Session> entry : activeSessions.entrySet()) {
            UUID uuid = entry.getKey();
            Session session = entry.getValue();
            Optional<Long> end = session.getValue(SessionKeys.END);
            if (!end.isPresent()) {
                session.endSession(now);
            }
            if (!database.isOpen()) {
                database.init();
            }
            try {
                database.save().session(uuid, session);
            } catch (DBOpException e) {
                errorHandler.log(L.ERROR, this.getClass(), e);
            }
        }
        activeSessions.clear();
    }
}
