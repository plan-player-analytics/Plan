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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.delivery.domain.keys.SessionKeys;
import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.ServerShutdownTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Class in charge of performing save operations when the server shuts down.
 *
 * @author AuroraLS3
 */
public abstract class ServerShutdownSave {

    protected final PluginLogger logger;
    private final DBSystem dbSystem;
    private final Locale locale;
    private final ErrorLogger errorLogger;

    private boolean shuttingDown = false;
    private boolean startedDatabase = false;

    protected ServerShutdownSave(
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorLogger errorLogger
    ) {
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.logger = logger;
        this.errorLogger = errorLogger;
    }

    protected abstract boolean checkServerShuttingDownStatus();

    public void serverIsKnownToBeShuttingDown() {
        shuttingDown = true;
    }

    public Optional<Future<?>> performSave() {
        if (!checkServerShuttingDownStatus() && !shuttingDown) {
            return Optional.empty();
        }

        Map<UUID, Session> activeSessions = SessionCache.getActiveSessions();
        if (activeSessions.isEmpty()) {
            return Optional.empty();
        }

        // This check ensures that logging is not attempted on JVM shutdown.
        // Underlying Logger might not be available leading to an exception.
        if (!shuttingDown) {
            logger.info(locale.getString(PluginLang.DISABLED_UNSAVED_SESSIONS));
        }
        return attemptSave(activeSessions);
    }

    private Optional<Future<?>> attemptSave(Map<UUID, Session> activeSessions) {
        try {
            prepareSessionsForStorage(activeSessions, System.currentTimeMillis());
            return Optional.of(saveActiveSessions(activeSessions));
        } catch (DBInitException e) {
            errorLogger.log(L.ERROR, e, ErrorContext.builder()
                    .whatToDo("Find the sessions in the error file and save them manually or ignore. Report & delete the error file after.")
                    .related("Shutdown save failed to init database.")
                    .related(activeSessions)
                    .build());
            return Optional.empty();
        } catch (IllegalStateException ignored) {
            /* Database is not initialized */
            return Optional.empty();
        } finally {
            closeDatabase(dbSystem.getDatabase());
        }
    }

    private Future<?> saveActiveSessions(Map<UUID, Session> activeSessions) {
        Database database = dbSystem.getDatabase();
        if (database.getState() == Database.State.CLOSED) {
            // Ensure that database is not closed when performing the transaction.
            startedDatabase = true;
            database.init();
        }

        return saveSessions(activeSessions, database);
    }

    void prepareSessionsForStorage(Map<UUID, Session> activeSessions, long now) {
        for (Session session : activeSessions.values()) {
            Optional<Long> end = session.getValue(SessionKeys.END);
            if (!end.isPresent()) {
                session.endSession(now);
            }
        }
    }

    private Future<?> saveSessions(Map<UUID, Session> activeSessions, Database database) {
        return database.executeTransaction(new ServerShutdownTransaction(activeSessions.values()));
    }

    private void closeDatabase(Database database) {
        if (startedDatabase) {
            database.close();
        }
    }
}