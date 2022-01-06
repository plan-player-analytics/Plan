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
package com.djrapitops.plan.utilities.logging;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.exceptions.ExceptionWithContext;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.java.Lists;
import com.djrapitops.plan.version.VersionChecker;
import dagger.Lazy;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * New logger that logs errors to specific files.
 *
 * @author AuroraLS3
 */
@Singleton
public class PluginErrorLogger implements ErrorLogger {

    private final PlanPlugin plugin;
    private final PluginLogger logger;
    private final PlanFiles files;
    private final Lazy<ServerProperties> serverProperties;
    private final Lazy<VersionChecker> versionChecker;
    private final Lazy<Formatters> formatters;

    @Inject
    public PluginErrorLogger(
            PlanPlugin plugin,
            PluginLogger logger,
            PlanFiles files,
            Lazy<ServerProperties> serverProperties,
            Lazy<VersionChecker> versionChecker,
            Lazy<Formatters> formatters
    ) {
        this.plugin = plugin;
        this.logger = logger;
        this.files = files;
        this.serverProperties = serverProperties;
        this.versionChecker = versionChecker;
        this.formatters = formatters;
    }

    @Override
    public void critical(Throwable throwable, ErrorContext context) {
        error(throwable, context);
        logger.error("CRITICAL error triggered a plugin shutdown.");
        plugin.onDisable();
    }

    @Override
    public void error(Throwable throwable, ErrorContext context) {
        log(logger::error, throwable, context);
    }

    @Override
    public void warn(Throwable throwable, ErrorContext context) {
        log(logger::warn, throwable, context);
    }

    private void log(Consumer<String> logMethod, Throwable throwable, ErrorContext context) {
        String errorName = throwable.getClass().getSimpleName();
        String hash = hash(throwable);
        Path logsDir = files.getLogsDirectory();
        Path errorLog = logsDir.resolve(errorName + "-" + hash + ".txt");

        mergeAdditionalContext(throwable, context);

        logToFile(errorLog, throwable, context, hash);
        for (String message : buildConsoleMessage(errorLog, throwable, context)) {
            logMethod.accept(message);
        }
    }

    private void logToFile(Path errorLog, Throwable throwable, ErrorContext context, String hash) {
        if (Files.exists(errorLog)) {
            logExisting(errorLog, throwable, context, hash);
        } else {
            logNew(errorLog, throwable, context, hash);
        }
    }

    private void mergeAdditionalContext(Throwable throwable, ErrorContext context) {
        Throwable cause = throwable;
        while (cause != null) {
            if (cause instanceof ExceptionWithContext) {
                ((ExceptionWithContext) cause).getContext().ifPresent(context::merge);
            }
            cause = cause.getCause();
        }
    }

    private void logExisting(Path errorLog, Throwable throwable, ErrorContext context, String hash) {
        // Read existing
        try (Stream<String> read = Files.lines(errorLog)) {
            List<String> lines = read.collect(Collectors.toList());

            int occurrences = getOccurrences(lines) + 1;
            List<String> newLines = buildNewLines(context, lines, occurrences, hash);
            overwrite(errorLog, throwable, newLines);
        } catch (IOException | IndexOutOfBoundsException e) {
            logAfterReadError(errorLog, throwable, context, hash);
        }
    }

    private void overwrite(Path errorLog, Throwable throwable, List<String> newLines) {
        try {
            Files.write(errorLog, newLines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throwable.addSuppressed(e);
            Logger.getGlobal().log(Level.SEVERE, "Failed to log Plan error, see suppressed.", throwable);
        }
    }

    private List<String> buildNewLines(ErrorContext context, List<String> lines, int occurrences, String hash) {
        Lists.Builder<String> builder = Lists.builder(String.class)
                .add(hash + " - Last occurred: " + getTimeStamp() + " Occurrences: " + occurrences);
        // 5 contexts are enough.
        if (occurrences <= 5) {
            builder = buildContext(context, occurrences, builder);
        }
        int lineCount = lines.size();
        int firstContextLineIndex = findFirstContextLine(lines, lineCount);
        return builder.addAll(lines.subList(firstContextLineIndex, lineCount))
                .build();
    }

    private String getTimeStamp() {
        return formatters.get().iso8601NoClockLong().apply(System.currentTimeMillis());
    }

    private Lists.Builder<String> buildContext(ErrorContext context, int occurrences, Lists.Builder<String> builder) {
        return builder.add("---- Context " + occurrences + " ----")
                .add("Plan v" + versionChecker.get().getCurrentVersion())
                .add(serverProperties.get().getName() + " " + serverProperties.get().getVersion())
                .add("Server v" + serverProperties.get().getImplVersion())
                .add("")
                .addAll(context.toLines())
                .add("");
    }

    private void logAfterReadError(Path errorLog, Throwable throwable, ErrorContext context, String hash) {
        logger.error("Failed to read " + errorLog + " deleting file");
        try {
            Files.deleteIfExists(errorLog);
        } catch (IOException ioException) {
            logger.error("Failed to delete " + errorLog);
        }
        logNew(errorLog, throwable, context, hash);
    }

    private int getOccurrences(List<String> lines) {
        if (lines.isEmpty()) return 0;

        String occurLine = lines.get(0);
        return Integer.parseInt(StringUtils.splitByWholeSeparator(occurLine, ": ")[2].trim());
    }

    private int findFirstContextLine(List<String> lines, int lineCount) {
        int firstContextLineIndex = 0;
        for (int i = 0; i < lineCount; i++) {
            if (lines.get(i).contains("---- Context")) {
                firstContextLineIndex = i;
                break;
            }
        }
        return firstContextLineIndex;
    }

    private String[] buildConsoleMessage(Path errorLog, Throwable throwable, ErrorContext context) {
        String errorName = throwable.getClass().getSimpleName();
        String errorMsg = throwable.getMessage();
        String errorLocation = errorLog.toString();
        return new String[]{
                "Ran into " + errorName + " - logged to " + errorLocation,
                "(INCLUDE CONTENTS OF THE FILE IN ANY REPORTS)",
                context.getWhatToDo().map(td -> "What to do: " + td).orElse("Error msg: \"" + errorMsg + "\"")
        };
    }

    private void logNew(Path errorLog, Throwable throwable, ErrorContext context, String hash) {
        List<String> stacktrace = buildReadableStacktrace(new ArrayList<>(), throwable);
        List<String> lines = Lists.builder(String.class)
                .add(hash + " - Last occurred: " + getTimeStamp() + " Occurrences: 1")
                .apply(builder -> this.buildContext(context, 1, builder))
                .add("---- Stacktrace ----")
                .addAll(stacktrace)
                .build();
        writeNew(errorLog, throwable, lines);
    }

    private void writeNew(Path errorLog, Throwable throwable, List<String> lines) {
        try {
            Path dir = errorLog.getParent();
            if (!Files.isSymbolicLink(dir)) Files.createDirectories(dir);
            Files.write(errorLog, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throwable.addSuppressed(e);
            Logger.getGlobal().log(Level.SEVERE, "Failed to log Plan error, see suppressed.", throwable);
        }
    }

    private String hash(Throwable e) {
        int seed = 0;
        Throwable cause = e;
        String previousLine = null;
        while (cause != null) {
            for (StackTraceElement element : cause.getStackTrace()) {
                String asLine = element.toString();
                if (asLine.equals(previousLine)) continue;
                if (seed == 0) {
                    seed = asLine.hashCode();
                } else {
                    seed *= asLine.hashCode();
                }
                previousLine = asLine;
            }
            cause = cause.getCause();
        }
        return DigestUtils.sha256Hex(Integer.toString(seed)).substring(0, 10);
    }

    private List<String> buildReadableStacktrace(List<String> trace, Throwable e) {
        trace.add(e.toString());
        Deduplicator deduplicator = new Deduplicator();
        for (StackTraceElement element : e.getStackTrace()) {
            String line = element.toString();
            deduplicator.addLines(trace, line);
        }
        deduplicator.addLeftoverDuplicateCountLine(trace);
        Throwable[] suppressed = e.getSuppressed();
        if (suppressed.length > 0) {
            for (Throwable suppressedThrowable : suppressed) {
                trace.add("   Suppressed:");
                for (String line : buildReadableStacktrace(new ArrayList<>(), suppressedThrowable)) {
                    trace.add("   " + line);
                }
            }
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            trace.add("Caused by:");
            buildReadableStacktrace(trace, cause);
        }
        return trace;
    }

    private static class Deduplicator {
        private String previousLine = null;
        private String lastDuplicate = null;
        private int duplicateCount = 0;

        public void addLines(List<String> trace, String line) {
            if (duplicateCount > 0 && !line.equals(lastDuplicate)) {
                String returnLine = "    x " + duplicateCount;
                duplicateCount = 1;
                trace.add(returnLine);
                trace.add("   " + line);
            } else if (line.equals(lastDuplicate)) {
                duplicateCount++;
            } else if (line.equals(previousLine)) {
                lastDuplicate = line;
                duplicateCount = 2;
            } else {
                previousLine = line;
                trace.add("   " + line);
            }
        }

        public void addLeftoverDuplicateCountLine(List<String> trace) {
            if (duplicateCount > 0) {
                trace.add("    x " + duplicateCount);
            }
        }
    }
}
