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
package com.djrapitops.plan.storage.json;

import com.djrapitops.plan.DebugChannels;
import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.debug.DebugLogger;
import com.djrapitops.plugin.task.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * In charge of storing json files on disk for later retrieval.
 *
 * @author Rsl1122
 */
@Singleton
public class JSONFileStorage implements JSONStorage {

    private final PluginLogger logger;

    private final Path jsonDirectory;

    private final Pattern timestampRegex = Pattern.compile(".*-([0-9]*).json");
    private static final String JSON_FILE_EXTENSION = ".json";
    private final DebugLogger debugLogger;

    private final Formatter<Long> dateFormatter;

    @Inject
    public JSONFileStorage(
            PlanFiles files,
            Formatters formatters,
            PluginLogger logger
    ) {
        this.logger = logger;
        debugLogger = logger.getDebugLogger();

        dateFormatter = formatters.yearLong();

        jsonDirectory = files.getJSONStorageDirectory();
    }

    @Override
    public StoredJSON storeJson(String identifier, String json, long timestamp) {
        Path writingTo = jsonDirectory.resolve(identifier + '-' + timestamp + JSON_FILE_EXTENSION);
        try {
            Files.createDirectories(jsonDirectory);
            Files.write(writingTo, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            logger.warn("Could not write a file to " + writingTo.toFile().getAbsolutePath() + ": " + e.getMessage());
        }
        return new StoredJSON(json, timestamp, dateFormatter);
    }

    @Override
    public Optional<StoredJSON> fetchJSON(String identifier) {
        File[] stored = jsonDirectory.toFile().listFiles();
        if (stored == null) return Optional.empty();
        for (File file : stored) {
            String fileName = file.getName();
            if (fileName.endsWith(JSON_FILE_EXTENSION) && fileName.startsWith(identifier)) {
                return Optional.ofNullable(readStoredJSON(file));
            }
        }
        return Optional.empty();
    }

    private StoredJSON readStoredJSON(File from) {
        Matcher timestampMatch = timestampRegex.matcher(from.getName());
        if (timestampMatch.find()) {
            try (Stream<String> lines = Files.lines(from.toPath())) {
                long timestamp = Long.parseLong(timestampMatch.group(1));
                StringBuilder json = new StringBuilder();
                lines.forEach(json::append);
                return new StoredJSON(json.toString(), timestamp, dateFormatter);
            } catch (IOException e) {
                logger.warn(jsonDirectory.toFile().getAbsolutePath() + " file '" + from.getName() + "' could not be read: " + e.getMessage());
            } catch (NumberFormatException e) {
                logger.warn(jsonDirectory.toFile().getAbsolutePath() + " contained a file '" + from.getName() + "' with improperly formatted -timestamp (could not parse number). This file was not placed there by Plan!");
            }
        } else {
            logger.warn(jsonDirectory.toFile().getAbsolutePath() + " contained a file '" + from.getName() + "' that has no -timestamp. This file was not placed there by Plan!");
        }
        return null;
    }

    @Override
    public Optional<StoredJSON> fetchExactJson(String identifier, long timestamp) {
        File found = jsonDirectory.resolve(identifier + "-" + timestamp + JSON_FILE_EXTENSION).toFile();
        if (!found.exists()) return Optional.empty();
        return Optional.ofNullable(readStoredJSON(found));
    }

    @Override
    public Optional<StoredJSON> fetchJsonMadeBefore(String identifier, long timestamp) {
        return fetchJSONWithTimestamp(identifier, timestamp, (timestampMatch, time) -> Long.parseLong(timestampMatch.group(1)) < time);
    }

    @Override
    public Optional<StoredJSON> fetchJsonMadeAfter(String identifier, long timestamp) {
        return fetchJSONWithTimestamp(identifier, timestamp, (timestampMatch, time) -> Long.parseLong(timestampMatch.group(1)) > time);
    }

    private Optional<StoredJSON> fetchJSONWithTimestamp(String identifier, long timestamp, BiPredicate<Matcher, Long> timestampComparator) {
        File[] stored = jsonDirectory.toFile().listFiles();
        if (stored == null) return Optional.empty();
        for (File file : stored) {
            try {
                String fileName = file.getName();
                if (fileName.endsWith(JSON_FILE_EXTENSION) && fileName.startsWith(identifier)) {
                    Matcher timestampMatch = timestampRegex.matcher(fileName);
                    if (timestampMatch.find() && timestampComparator.test(timestampMatch, timestamp)) {
                        return Optional.ofNullable(readStoredJSON(file));
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore this file, malformed timestamp
            }
        }
        return Optional.empty();
    }

    @Override
    public void invalidateOlder(String identifier, long timestamp) {
        File[] stored = jsonDirectory.toFile().listFiles();
        if (stored == null) return;

        List<File> toDelete = new ArrayList<>();
        for (File file : stored) {
            try {
                String fileName = file.getName();
                if (fileName.endsWith(JSON_FILE_EXTENSION) && fileName.startsWith(identifier)) {
                    Matcher timestampMatch = timestampRegex.matcher(fileName);
                    if (timestampMatch.find() && Long.parseLong(timestampMatch.group(1)) < timestamp) {
                        toDelete.add(file);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore this file, malformed timestamp
            }
        }
        deleteFiles(toDelete);
    }

    private void invalidateOlderButIgnore(long timestamp, String... ignoredIdentifiers) {
        File[] stored = jsonDirectory.toFile().listFiles();
        if (stored == null) return;

        List<File> toDelete = new ArrayList<>();
        outer:
        for (File file : stored) {
            try {
                String fileName = file.getName();
                if (fileName.endsWith(JSON_FILE_EXTENSION)) {
                    Matcher timestampMatch = timestampRegex.matcher(fileName);
                    if (timestampMatch.find() && Long.parseLong(timestampMatch.group(1)) < timestamp) {
                        for (String ignoredIdentifier : ignoredIdentifiers) {
                            if (fileName.startsWith(ignoredIdentifier)) continue outer;
                        }
                        toDelete.add(file);
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore this file, malformed timestamp
            }
        }

        deleteFiles(toDelete);
    }

    private void deleteFiles(List<File> toDelete) {
        for (File fileToDelete : toDelete) {
            try {
                debugLogger.logOn(DebugChannels.JSON_CACHE, "Deleting " + fileToDelete.getAbsolutePath());
                Files.delete(fileToDelete.toPath());
            } catch (IOException e) {
                // Failed to delete, set for deletion on next server shutdown.
                fileToDelete.deleteOnExit();
            }
        }
    }

    @Singleton
    public static class CleanTask extends TaskSystem.Task {
        private final PlanConfig config;
        private final JSONFileStorage jsonFileStorage;
        private final DebugLogger debugLogger;

        @Inject
        public CleanTask(
                PlanConfig config,
                JSONFileStorage jsonFileStorage,
                DebugLogger debugLogger
        ) {
            this.config = config;
            this.jsonFileStorage = jsonFileStorage;
            this.debugLogger = debugLogger;
        }

        @Override
        public void register(RunnableFactory runnableFactory) {
            long delay = TimeAmount.toTicks(ThreadLocalRandom.current().nextInt(60), TimeUnit.SECONDS);
            long period = TimeAmount.toTicks(1, TimeUnit.HOURS);
            runnableFactory.create(null, this).runTaskTimerAsynchronously(delay, period);
        }

        @Override
        public void run() {
            long now = System.currentTimeMillis();
            long invalidateDiskCacheAfterMs = config.get(WebserverSettings.INVALIDATE_DISK_CACHE);
            long invalidateQueriesAfterMs = config.get(WebserverSettings.INVALIDATE_QUERY_RESULTS);
            debugLogger.logOn(DebugChannels.JSON_CACHE, "Running clean task..");

            jsonFileStorage.invalidateOlder("query", now - invalidateQueriesAfterMs);
            jsonFileStorage.invalidateOlderButIgnore(now - invalidateDiskCacheAfterMs, "query");
        }

        public DebugLogger getDebugLogger() {
            return debugLogger;
        }
    }
}
