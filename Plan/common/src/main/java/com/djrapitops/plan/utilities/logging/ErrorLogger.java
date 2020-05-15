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
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import dagger.Lazy;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * New logger that logs errors to specific files.
 *
 * @author Rsl1122
 */
@Singleton
public class ErrorLogger implements ErrorHandler {

    private final PlanPlugin plugin;
    private final PluginLogger logger;
    private final PlanFiles files;
    private final Lazy<ServerProperties> serverProperties;
    private final Lazy<VersionChecker> versionChecker;
    private final Lazy<Formatters> formatters;

    @Inject
    public ErrorLogger(
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

    public <T extends ExceptionWithContext> void log(L level, T throwable) {
        log(level, (Throwable) throwable, throwable.getContext().orElse(ErrorContext.builder().related("Missing Context").build()));
    }

    public void log(L level, Throwable throwable, ErrorContext context) {
        String errorName = throwable.getClass().getSimpleName();
        String hash = hash(throwable);
        Path logsDir = files.getLogsDirectory();
        Path errorLog = logsDir.resolve(errorName + "-" + hash + ".txt");
        mergeAdditionalContext(throwable, context);
        if (Files.exists(errorLog)) {
            logExisting(errorLog, throwable, context);
        } else {
            logNew(errorLog, throwable, context);
        }
        logToConsole(level, errorLog, throwable, context);
        if (L.CRITICAL == level) {
            plugin.getPluginLogger().error("CRITICAL error triggered a plugin shutdown.");
            plugin.onDisable();
        }
    }

    public void mergeAdditionalContext(Throwable throwable, ErrorContext context) {
        Throwable cause = throwable.getCause();
        while (cause != null) {
            if (cause instanceof ExceptionWithContext) {
                ((ExceptionWithContext) cause).getContext().ifPresent(context::merge);
            }
            cause = cause.getCause();
        }
    }

    private void logExisting(Path errorLog, Throwable throwable, ErrorContext context) {
        // Read existing
        List<String> lines;
        try (Stream<String> read = Files.lines(errorLog)) {
            lines = read.collect(Collectors.toList());
        } catch (IOException e) {
            logAfterReadError(errorLog, throwable, context);
            return;
        }
        int occurrences = getOccurrences(lines) + 1;
        List<String> newLines = buildNewLines(context, lines, occurrences);
        overwrite(errorLog, throwable, newLines);
    }

    private void overwrite(Path errorLog, Throwable throwable, List<String> newLines) {
        try {
            Files.write(errorLog, newLines, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throwable.addSuppressed(e);
            Logger.getGlobal().log(Level.SEVERE, "Failed to log Plan error, see suppressed.", throwable);
        }
    }

    private List<String> buildNewLines(ErrorContext context, List<String> lines, int occurrences) {
        Lists.Builder<String> builder = Lists.builder(String.class)
                .add("Last occurred: " + getTimeStamp() + " Occurrences: " + occurrences);
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

    private void logAfterReadError(Path errorLog, Throwable throwable, ErrorContext context) {
        logger.error("Failed to read " + errorLog + " deleting file");
        try {
            Files.deleteIfExists(errorLog);
        } catch (IOException ioException) {
            logger.error("Failed to delete " + errorLog);
        }
        logNew(errorLog, throwable, context);
    }

    private int getOccurrences(List<String> lines) {
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

    private void logToConsole(L level, Path errorLog, Throwable throwable, ErrorContext context) {
        String errorName = throwable.getClass().getSimpleName();
        String errorMsg = throwable.getMessage();
        String errorLocation = errorLog.toString();
        logger.log(level,
                "Ran into " + errorName + " - logged to " + errorLocation,
                "(INCLUDE CONTENTS OF THE FILE IN ANY REPORTS)",
                context.getWhatToDo().map(td -> "What to do: " + td).orElse("Error msg: \"" + errorMsg + "\"")
        );
    }

    private void logNew(Path errorLog, Throwable throwable, ErrorContext context) {
        List<String> stacktrace = buildReadableStacktrace(throwable);
        List<String> lines = Lists.builder(String.class)
                .add("Last occurred: " + getTimeStamp() + " Occurrences: 1")
                .apply(builder -> this.buildContext(context, 1, builder))
                .add("---- Stacktrace ----")
                .addAll(stacktrace)
                .build();
        writeNew(errorLog, throwable, lines);
    }

    private void writeNew(Path errorLog, Throwable throwable, List<String> lines) {
        try {
            Files.createDirectories(errorLog.getParent());
            Files.write(errorLog, lines, StandardOpenOption.CREATE_NEW, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throwable.addSuppressed(e);
            Logger.getGlobal().log(Level.SEVERE, "Failed to log Plan error, see suppressed.", throwable);
        }
    }

    @Override
    @Deprecated
    public void log(L level, Class caughtBy, Throwable throwable) {
        log(level, throwable, ErrorContext.builder()
                .related("Caught by " + caughtBy.getName())
                .build());
    }

    private String hash(Throwable e) {
        StringBuilder seed = new StringBuilder();
        Throwable cause = e;
        Set<String> alreadyPresent = new HashSet<>();
        while (cause != null) {
            for (StackTraceElement element : cause.getStackTrace()) {
                String asLine = element.toString();
                if (!alreadyPresent.contains(asLine)) {
                    seed.append(asLine);
                }
                alreadyPresent.add(asLine);
            }
            cause = e.getCause();
        }
        return DigestUtils.sha256Hex(seed.toString()).substring(0, 10);
    }

    private List<String> buildReadableStacktrace(Throwable e) {
        List<String> trace = new ArrayList<>();
        trace.add(e.toString());
        for (StackTraceElement element : e.getStackTrace()) {
            trace.add("   " + element);
        }
        Throwable[] suppressed = e.getSuppressed();
        if (suppressed.length > 0) {
            for (Throwable suppressedThrowable : suppressed) {
                trace.add("   Suppressed:");
                buildReadableStacktrace(suppressedThrowable).stream().map(line -> "   " + line).forEach(trace::add);
            }
        }
        Throwable cause = e.getCause();
        if (cause != null) {
            trace.add("Caused by:");
            trace.addAll(buildReadableStacktrace(cause));
        }
        return trace;
    }
}
