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

import com.djrapitops.plan.exceptions.database.DBInitException;
import com.djrapitops.plan.gathering.afk.AFKTracker;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.transactions.events.ServerShutdownTransaction;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.server.PluginLogger;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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

    public abstract Optional<AFKTracker> getAfkTracker();

    public void serverIsKnownToBeShuttingDown() {
        shuttingDown = true;
    }

    public Optional<Future<?>> performSave() {
        if (!checkServerShuttingDownStatus() && !shuttingDown) {
            return Optional.empty();
        }

        Collection<ActiveSession> activeSessions = SessionCache.getActiveSessions();
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

    private Optional<Future<?>> attemptSave(Collection<ActiveSession> activeSessions) {
        try {
            return saveActiveSessions(finishSessions(activeSessions, System.currentTimeMillis()));
        } catch (DBInitException e) {
            errorLogger.error(e, ErrorContext.builder()
                    .whatToDo("Find the sessions in the error file and save them manually or ignore. Report & delete the error file after.")
                    .related("Shutdown save failed to init database.")
                    .related(activeSessions)
                    .build());
            return Optional.empty();
        } catch (IllegalStateException ignored) {
            /* Database is not initialized */
            return Optional.empty();
        }
    }

    private Optional<Future<?>> saveActiveSessions(Collection<FinishedSession> finishedSessions) {
        Database database = dbSystem.getDatabase();
        if (database.getState() == Database.State.CLOSED) {
            // Don't attempt to save if database is closed, session storage will be handled by
            // ShutdownDataPreservation instead.
            // Previously database reboot was attempted, but this could lead to server hang.
            return Optional.empty();
        }

        return Optional.of(saveSessions(finishedSessions, database));
    }

    Collection<FinishedSession> finishSessions(Collection<ActiveSession> activeSessions, long now) {
        return activeSessions.stream().map(session -> {
            getAfkTracker().ifPresent(afkTracker -> afkTracker.performedAction(session.getPlayerUUID(), now));
            return session.toFinishedSession(now);
        }).collect(Collectors.toList());
    }

    private Future<?> saveSessions(Collection<FinishedSession> finishedSessions, Database database) {
        return database.executeTransaction(new ServerShutdownTransaction(finishedSessions));
    }
}