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
package com.djrapitops.plan.system.gathering;

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.exceptions.database.DBOpException;
import com.djrapitops.plan.system.gathering.cache.SessionCache;
import com.djrapitops.plan.system.gathering.domain.Session;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plan.system.settings.locale.lang.PluginLang;
import com.djrapitops.plan.system.storage.database.DBSystem;
import com.djrapitops.plan.system.storage.database.Database;
import com.djrapitops.plan.system.storage.database.transactions.events.ServerShutdownTransaction;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Class in charge of performing save operations when the server shuts down.
 *
 * @author Rsl1122
 */
public abstract class ServerShutdownSave {

    protected final PluginLogger logger;
    private final DBSystem dbSystem;
    private final Locale locale;
    private final ErrorHandler errorHandler;
    private boolean shuttingDown = false;

    public ServerShutdownSave(
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.logger = logger;
        this.errorHandler = errorHandler;
    }

    protected abstract boolean checkServerShuttingDownStatus();

    public void serverIsKnownToBeShuttingDown() {
        shuttingDown = true;
    }

    public void performSave() {
        if (!checkServerShuttingDownStatus() && !shuttingDown) {
            return;
        }

        Map<UUID, Session> activeSessions = SessionCache.getActiveSessions();
        if (activeSessions.isEmpty()) {
            return;
        }

        // This check ensures that logging is not attempted on JVM shutdown.
        // Underlying Logger might not be available leading to an exception.
        if (!shuttingDown) {
            logger.info(locale.getString(PluginLang.DISABLED_UNSAVED_SESSIONS));
        }
        attemptSave(activeSessions);

        SessionCache.clear();
    }

    private void attemptSave(Map<UUID, Session> activeSessions) {
        try {
            prepareSessionsForStorage(activeSessions, System.currentTimeMillis());
            saveActiveSessions(activeSessions);
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
            database.executeTransaction(new ServerShutdownTransaction(activeSessions.values()))
                    .get(); // Ensure that the transaction is executed before shutdown.
        } catch (ExecutionException | DBOpException e) {
            errorHandler.log(L.ERROR, this.getClass(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeDatabase(Database database) {
        database.close();
    }
}