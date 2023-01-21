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
package com.djrapitops.plan.utilities.dev;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utilities.LogStoringHandler;

import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test to increase coverage on new code and pass sonar.
 *
 * @author AuroraLS3
 */
class BenchmarkTest {

    LogStoringHandler logHandler;

    @BeforeEach
    void registerLogHandler() {
        logHandler = new LogStoringHandler();
        Logger.getLogger("Plan").addHandler(logHandler);
    }

    @AfterEach
    void clearLogHandler() {
        logHandler.close();
    }

    @Test
    void benchmarkOnRunnable() {
        Benchmark.bench(() -> {});
        List<String> records = logHandler.getRecords().stream()
                .map(LogRecord::getMessage)
                .toList();
        assertEquals(1, records.size());
        assertTrue(records.get(0).contains("woah") || records.get(0).contains("fast"));
    }

    @Test
    void benchmarkOnSupplier() {
        boolean result = Benchmark.bench(() -> true);
        assertTrue(result);
        List<String> records = logHandler.getRecords().stream()
                .map(LogRecord::getMessage)
                .toList();
        assertEquals(1, records.size());
        assertTrue(records.get(0).contains("woah") || records.get(0).contains("fast"));
    }

}