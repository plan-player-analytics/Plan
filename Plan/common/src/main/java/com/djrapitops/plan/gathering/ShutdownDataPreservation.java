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

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.FinishedSession;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.PluginLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.transactions.events.ShutdownDataPreservationTransaction;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.text.TextStringBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ShutdownDataPreservation extends TaskSystem.Task {

    private final Locale locale;
    private final DBSystem dbSystem;
    private final PluginLogger logger;
    private final ErrorLogger errorLogger;
    private final ServerShutdownSave serverShutdownSave;

    private final Path storeLocation;

    @Inject
    public ShutdownDataPreservation(
            PlanFiles files,
            Locale locale,
            DBSystem dbSystem,
            PluginLogger logger,
            ErrorLogger errorLogger,
            ServerShutdownSave serverShutdownSave
    ) {
        this.locale = locale;
        this.dbSystem = dbSystem;

        storeLocation = files.getDataDirectory().resolve("unsaved-sessions.csv");
        this.logger = logger;
        this.errorLogger = errorLogger;
        this.serverShutdownSave = serverShutdownSave;
    }

    public void storePreviouslyPreservedSessions() {
        if (storeLocation.toFile().exists()) {
            try {
                logger.info(locale.getString(PluginLang.ENABLE_NOTIFY_STORING_PRESERVED_SESSIONS));
                List<FinishedSession> finishedSessions = loadFinishedSessions();
                storeInDB(finishedSessions);
                deleteStorageFile();
            } catch (IllegalStateException e) {
                errorLogger.error(e, ErrorContext.builder().related(storeLocation).build());
            }
        }
    }

    private void storeInDB(List<FinishedSession> finishedSessions) {
        if (!finishedSessions.isEmpty()) {
            try {
                dbSystem.getDatabase().executeTransaction(new ShutdownDataPreservationTransaction(finishedSessions)).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                throw new IllegalStateException("Failed to store unsaved sessions in database: " + e.getCause().getMessage(), e);
            }
        }
    }

    @Override
    public void run() {
        try {
            storePreviouslyPreservedSessions();
        } finally {
            cancel();
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        runnableFactory.create(this).runTaskAsynchronously();
    }

    private void deleteStorageFile() {
        try {
            Files.delete(storeLocation);
        } catch (IOException e) {
            throw new IllegalStateException("Could not delete " + storeLocation.toFile().getAbsolutePath() + ", " + e.getMessage());
        }
    }

    List<FinishedSession> loadFinishedSessions() {
        if (!Files.exists(storeLocation)) return Collections.emptyList();
        try (Stream<String> lines = Files.lines(storeLocation)) {
            return lines.map(this::deserializeToSession)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + storeLocation.toFile().getAbsolutePath() + ", " + e.getMessage(), e);
        }
    }

    private Optional<FinishedSession> deserializeToSession(String line) {
        try {
            return FinishedSession.deserializeCSV(line);
        } catch (Exception e) {
            logger.warn("Ignoring line '" + line + "' in unsaved-sessions.csv due to: " + e);
            return Optional.empty();
        }
    }

    public void preserveSessionsInCache() {
        long now = System.currentTimeMillis();
        List<FinishedSession> finishedSessions = SessionCache.getActiveSessions().stream()
                .map(session -> {
                    serverShutdownSave.getAfkTracker().ifPresent(afkTracker -> afkTracker.performedAction(session.getPlayerUUID(), now));
                    return session.toFinishedSession(now);
                })
                .collect(Collectors.toList());
        storeFinishedSessions(finishedSessions);
    }

    void storeFinishedSessions(List<FinishedSession> sessions) {
        if (sessions.isEmpty()) return;
        if (storeLocation.toFile().exists()) {
            sessions.addAll(loadFinishedSessions());
        }

        List<String> lines = sessions.stream().map(FinishedSession::serializeCSV).collect(Collectors.toList());
        try {
            Files.write(storeLocation, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            logger.warn("Place this to " + storeLocation.toFile().getAbsolutePath() + " if you wish to store missing sessions:");
            logger.warn(new TextStringBuilder().appendWithSeparators(lines, "\n").build());
            throw new IllegalStateException("Could not write " + storeLocation.toFile().getAbsolutePath() + ", " + e.getMessage(), e);
        }
    }
}
