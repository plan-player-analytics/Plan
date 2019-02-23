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
package com.djrapitops.plan;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.api.exceptions.database.DBOpException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.keys.SessionKeys;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.events.ServerShutdownTransaction;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.DBSystem;
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
    private final DBSystem dbSystem;
    private final ErrorHandler errorHandler;

    @Inject
    public ShutdownHook(DBSystem dbSystem, ErrorHandler errorHandler) {
        this.dbSystem = dbSystem;
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
            if (activeSessions.isEmpty()) {
                return;
            }

            prepareSessionsForStorage(activeSessions, System.currentTimeMillis());
            saveActiveSessions(activeSessions);
            SessionCache.clear();
        } catch (DBInitException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (IllegalStateException ignored) {
            /* Database is not initialized */
        } finally {
            closeDatabase(dbSystem.getDatabase());
        }
    }

    private void saveActiveSessions(Map<UUID, Session> activeSessions) {
        Database database = dbSystem.getDatabase();
        if (database.getState() == Database.State.CLOSED) {
            // Ensure that database is not closed when performing the transaction.
            database.init();
        }

        saveSessions(activeSessions, database);
    }

    private void prepareSessionsForStorage(Map<UUID, Session> activeSessions, long now) {
        for (Session session : activeSessions.values()) {
            Optional<Long> end = session.getValue(SessionKeys.END);
            if (!end.isPresent()) {
                session.endSession(now);
            }
        }
    }

    private void saveSessions(Map<UUID, Session> activeSessions, Database database) {
        try {
            database.executeTransaction(new ServerShutdownTransaction(activeSessions.values()));
        } catch (DBOpException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        }
    }

    private void closeDatabase(Database database) {
        database.close();
    }
}
