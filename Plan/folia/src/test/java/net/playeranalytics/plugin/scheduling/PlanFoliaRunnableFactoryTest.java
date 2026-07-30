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
package net.playeranalytics.plugin.scheduling;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanFoliaRunnableFactoryTest {

    @Mock
    private JavaPlugin plugin;
    @Mock
    private Server server;
    @Mock
    private AsyncScheduler asyncScheduler;
    @Mock
    private GlobalRegionScheduler globalRegionScheduler;
    @Mock
    private ScheduledTask scheduledTask;

    private PlanFoliaRunnableFactory factory;

    @BeforeEach
    void setUp() {
        when(plugin.getServer()).thenReturn(server);
        when(server.getAsyncScheduler()).thenReturn(asyncScheduler);
        when(server.getGlobalRegionScheduler()).thenReturn(globalRegionScheduler);
        factory = new PlanFoliaRunnableFactory(plugin);
    }

    @Test
    void convertsTicksToMillisecondsForAsyncTasks() {
        when(asyncScheduler.runDelayed(eq(plugin), any(), anyLong(), any())).thenReturn(scheduledTask);
        when(asyncScheduler.runAtFixedRate(eq(plugin), any(), anyLong(), anyLong(), any())).thenReturn(scheduledTask);

        factory.create(() -> {}).runTaskLaterAsynchronously(20L);
        factory.create(() -> {}).runTaskTimerAsynchronously(40L, 60L);

        verify(asyncScheduler).runDelayed(eq(plugin), any(), eq(1_000L), eq(TimeUnit.MILLISECONDS));
        verify(asyncScheduler).runAtFixedRate(
                eq(plugin), any(), eq(2_000L), eq(3_000L), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void cancelledLifecycleTasksDoNotRunAfterReload() {
        when(asyncScheduler.runNow(eq(plugin), any())).thenReturn(scheduledTask);
        AtomicInteger executions = new AtomicInteger();
        ArgumentCaptor<Consumer<ScheduledTask>> callbacks = consumerCaptor();

        factory.create(executions::incrementAndGet).runTaskAsynchronously();
        factory.cancelAllKnownTasks();
        factory.create(executions::incrementAndGet).runTaskAsynchronously();
        verify(asyncScheduler, org.mockito.Mockito.times(2)).runNow(eq(plugin), callbacks.capture());

        List<Consumer<ScheduledTask>> capturedCallbacks = callbacks.getAllValues();
        capturedCallbacks.get(0).accept(scheduledTask);
        capturedCallbacks.get(1).accept(scheduledTask);

        assertEquals(1, executions.get());
        verify(asyncScheduler).cancelTasks(plugin);
        verify(globalRegionScheduler).cancelTasks(plugin);
    }

    @Test
    void cancellationWaitsForRunningTaskBeforeReturning() throws Exception {
        when(asyncScheduler.runNow(eq(plugin), any())).thenReturn(scheduledTask);
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ArgumentCaptor<Consumer<ScheduledTask>> callback = consumerCaptor();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            factory.create(() -> {
                started.countDown();
                await(release);
            }).runTaskAsynchronously();
            verify(asyncScheduler).runNow(eq(plugin), callback.capture());

            Future<?> task = executor.submit(() -> callback.getValue().accept(scheduledTask));
            assertTrue(started.await(1L, TimeUnit.SECONDS));
            Future<?> cancellation = executor.submit(factory::cancelAllKnownTasks);
            verify(asyncScheduler, org.mockito.Mockito.timeout(1_000L)).cancelTasks(plugin);

            assertThrows(TimeoutException.class, () -> cancellation.get(100L, TimeUnit.MILLISECONDS));
            release.countDown();
            task.get(1L, TimeUnit.SECONDS);
            cancellation.get(1L, TimeUnit.SECONDS);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void cancellationFromRunningTaskDoesNotDeadlock() throws Exception {
        when(asyncScheduler.runNow(eq(plugin), any())).thenReturn(scheduledTask);
        ArgumentCaptor<Consumer<ScheduledTask>> callback = consumerCaptor();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            factory.create(factory::cancelAllKnownTasks).runTaskAsynchronously();
            verify(asyncScheduler).runNow(eq(plugin), callback.capture());

            Future<?> task = executor.submit(() -> callback.getValue().accept(scheduledTask));
            task.get(1L, TimeUnit.SECONDS);
            verify(asyncScheduler).cancelTasks(plugin);
            verify(globalRegionScheduler).cancelTasks(plugin);
        } finally {
            executor.shutdownNow();
        }
    }

    @SuppressWarnings("unchecked")
    private static ArgumentCaptor<Consumer<ScheduledTask>> consumerCaptor() {
        return ArgumentCaptor.forClass(Consumer.class);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
