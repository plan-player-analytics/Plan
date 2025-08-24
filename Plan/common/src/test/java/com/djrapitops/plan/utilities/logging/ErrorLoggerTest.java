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

import com.djrapitops.plan.PlanSystem;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorLoggerTest {

    private static ErrorLogger UNDER_TEST;
    private static Path LOGS_DIR;
    private static PlanSystem SYSTEM;

    @BeforeAll
    static void preparePlugin(@TempDir Path dir) throws Exception {
        PluginMockComponent component = new PluginMockComponent(dir);
        SYSTEM = component.getPlanSystem();
        SYSTEM.enable();
        UNDER_TEST = component.getPluginErrorLogger();
        LOGS_DIR = SYSTEM.getPlanFiles().getLogsDirectory();
    }

    @AfterAll
    static void tearDownPlugin() {
        SYSTEM.disable();
    }

    @BeforeEach
    void clearLogsDir() throws IOException {
        File[] files = LOGS_DIR.toFile().listFiles();
        if (files != null) {
            for (File file : files) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }

    private File[] getErrorLogFiles() {
        File[] files = LOGS_DIR.toFile().listFiles();
        return files != null ? files : new File[]{};
    }

    private List<String> getLines(String errorName) throws IOException {
        File[] errorLogFiles = getErrorLogFiles();
        for (File logFile : errorLogFiles) {
            if (logFile.getName().contains(errorName)) {
                try (Stream<String> lines = Files.lines(logFile.toPath())) {
                    return lines.collect(Collectors.toList());
                }
            }
        }
        throw new AssertionError("File with name '" + errorName + "' doesn't exist: " + Arrays.toString(errorLogFiles));
    }

    @Test
    void simpleErrorIsLogged() throws IOException {

        IllegalStateException error = new IllegalStateException();
        assertTimeoutPreemptively(Duration.ofMillis(500), () ->
                UNDER_TEST.warn(error, ErrorContext.builder()
                        .whatToDo("Succeed the test")
                        .related("Test object")
                        .build())
        );

        List<String> lines = getLines(error.getClass().getSimpleName());
        assertTrue(lines.contains("java.lang.IllegalStateException"), () -> "Did not contain 'java.lang.IllegalStateException', " + lines);
    }

    @Test
    void errorWithACauseIsLogged() throws IOException {
        IllegalStateException error = new IllegalStateException(
                new IllegalArgumentException(
                        new NullPointerException()
                )
        );

        assertTimeoutPreemptively(Duration.ofMillis(500), () ->
                UNDER_TEST.warn(error, ErrorContext.builder()
                        .whatToDo("Succeed the test")
                        .related("Test object")
                        .build())
        );

        List<String> lines = getLines(error.getClass().getSimpleName());
        assertTrue(lines.contains("java.lang.IllegalStateException: java.lang.IllegalArgumentException: java.lang.NullPointerException"), () -> "Did not contain 'java.lang.IllegalStateException: java.lang.IllegalArgumentException: java.lang.NullPointerException', " + lines);
        assertTrue(lines.contains("Caused by:"), () -> "Did not contain 'Caused by:', " + lines);
        assertTrue(lines.contains("java.lang.IllegalArgumentException: java.lang.NullPointerException"), () -> "Did not contain 'java.lang.IllegalArgumentException: java.lang.NullPointerException', " + lines);
        assertTrue(lines.contains("java.lang.NullPointerException"), () -> "Did not contain 'caused by: java.lang.NullPointerException', " + lines);
    }

    @Test
    void errorWithSuppressedIsLogged() throws IOException {
        IllegalStateException error = new IllegalStateException();
        error.addSuppressed(new IllegalArgumentException());
        error.addSuppressed(new NullPointerException());

        assertTimeoutPreemptively(Duration.of(5, ChronoUnit.SECONDS), () ->
                UNDER_TEST.warn(error, ErrorContext.builder()
                        .whatToDo("Succeed the test")
                        .related("Test object")
                        .build())
        );

        List<String> lines = getLines(error.getClass().getSimpleName());
        assertTrue(lines.contains("java.lang.IllegalStateException"), () -> "Did not contain 'java.lang.IllegalStateException', " + lines);
        assertTrue(lines.contains("   Suppressed:"), () -> "Did not contain '   Suppressed:', " + lines);
        assertTrue(lines.contains("   java.lang.IllegalArgumentException"), () -> "Did not contain '   java.lang.IllegalArgumentException', " + lines);
        assertTrue(lines.contains("   java.lang.NullPointerException"), () -> "Did not contain '   java.lang.NullPointerException', " + lines);
    }
}