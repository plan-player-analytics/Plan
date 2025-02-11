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
package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.awaitility.Awaitility;
import utilities.TestConstants;
import utilities.mocks.PluginMockComponent;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerFileLoaderTest {
    static PlanSystem system;
    static ServerLoader underTest;
    private static ServerUUID serverUUID;

    @BeforeAll
    static void setUp(@TempDir Path tempDir) throws Exception {
        PluginMockComponent mockComponent = new PluginMockComponent(tempDir);
        system = mockComponent.getPlanSystem();
        system.enable();
    }

    @AfterAll
    static void tearDown() {
        if (system != null) system.disable();
    }

    @BeforeEach
    void setUpEach() {
        underTest = new AtomicServerLoader(
                new ServerFileLoader(TestConstants.VERSION, system.getPlanFiles(), system.getConfigSystem().getConfig())
        );
        Optional<Server> loaded = underTest.load(null);
        assertTrue(loaded.isPresent());

        if (serverUUID == null) {
            serverUUID = loaded.get().getUuid();
        }
    }

    @Test
    void runParallelLoadsAndSaves() throws InterruptedException {
        ExecutorService executorService = new ScheduledThreadPoolExecutor(6);

        AtomicInteger runs = new AtomicInteger(1);
        int expected = 1000;
        AtomicInteger fails = new AtomicInteger(0);
        try {
            for (int i = 0; i < expected; i++) {
                executorService.submit(() -> {
                    try {
                        Optional<Server> load = underTest.load(null);
                        if (load.isPresent()) {
                            underTest.save(load.get());
                        } else {
                            System.out.println("Failure " + fails.incrementAndGet());
                        }
                    } finally {
                        runs.incrementAndGet();
                    }
                });
            }
            Awaitility.await()
                    .atMost(2, TimeUnit.MINUTES)
                    .until(() -> runs.get() >= expected);
        } finally {
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        }
        assertEquals(0, fails.get());
    }
}
