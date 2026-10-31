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
package com.djrapitops.plan.addons.placeholderapi;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.placeholder.PlanPlaceholders;
import com.djrapitops.plan.processing.Processing;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import com.djrapitops.plan.version.VersionChecker;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PlanPlaceholderExtensionTest {

    private PlanPlaceholderExtension underTest;
    private AtomicInteger calculations;
    private OfflinePlayer player;

    @BeforeEach
    void setUp() {
        calculations = new AtomicInteger();
        PlanPlaceholders placeholders = mock(PlanPlaceholders.class);
        when(placeholders.onPlaceholderRequest(any(), eq("player_join_address"), anyList()))
                .thenAnswer(invocation -> "play.example.com#" + calculations.incrementAndGet());

        Processing processing = mock(Processing.class);
        when(processing.submitNonCritical(any(Runnable.class))).thenAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run(); // Run synchronously for the test
            return CompletableFuture.completedFuture(true);
        });
        PlanSystem system = mock(PlanSystem.class);
        when(system.getProcessing()).thenReturn(processing);
        when(system.getVersionChecker()).thenReturn(mock(VersionChecker.class));

        underTest = new PlanPlaceholderExtension(placeholders, system, mock(ErrorLogger.class));

        player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
    }

    private String requestOnServerThread() throws InterruptedException {
        AtomicReference<String> result = new AtomicReference<>();
        Thread serverThread = new Thread(() -> result.set(underTest.onRequest(player, "player_join_address")), "Server thread");
        serverThread.start();
        serverThread.join();
        return result.get();
    }

    @Test
    void freshCachedValueIsNotRecalculatedOnServerThread() throws InterruptedException {
        assertNull(requestOnServerThread()); // Nothing cached yet, calculation is started
        assertEquals(1, calculations.get());

        for (int i = 0; i < 10; i++) {
            assertEquals("play.example.com#1", requestOnServerThread());
        }
        assertEquals(1, calculations.get(), "Fresh value should be served from cache without recalculating");
    }

    @Test
    void refreshIsNeededOnlyWhenMissingOrOld() {
        long now = 1_000_000L;
        assertTrue(PlanPlaceholderExtension.needsRefresh(null, now));
        assertFalse(PlanPlaceholderExtension.needsRefresh(
                new PlanPlaceholderExtension.CachedValue("x", now), now));
        assertFalse(PlanPlaceholderExtension.needsRefresh(
                new PlanPlaceholderExtension.CachedValue("x", now - PlanPlaceholderExtension.REFRESH_AFTER_MS + 1), now));
        assertTrue(PlanPlaceholderExtension.needsRefresh(
                new PlanPlaceholderExtension.CachedValue("x", now - PlanPlaceholderExtension.REFRESH_AFTER_MS), now));
        assertTrue(PlanPlaceholderExtension.REFRESH_AFTER_MS < PlanPlaceholderExtension.EXPIRE_AFTER_MS,
                "Values must be refreshed before they expire");
    }
}
