package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.shaded.org.awaitility.Awaitility;
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
    static ServerFileLoader underTest;
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
        underTest = new ServerFileLoader(TestConstants.VERSION, system.getPlanFiles(), system.getConfigSystem().getConfig());
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
        int expected = 10000;
        AtomicInteger fails = new AtomicInteger(0);
        try {
            for (int i = 0; i < expected; i++) {
                executorService.submit(() -> {
                    Optional<Server> load = underTest.load(null);
                    if (load.isPresent()) {
                        underTest.save(load.get());
                    } else {
                        System.out.println("Failure " + fails.incrementAndGet());
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