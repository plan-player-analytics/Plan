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
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Folia runnable factory that prevents tasks from an old Plan lifecycle from running after reload.
 */
public class PlanFoliaRunnableFactory implements RunnableFactory {

    private final JavaPlugin plugin;
    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalRegionScheduler;
    private final AtomicLong lifecycle = new AtomicLong();
    private final ReentrantLock lifecycleLock = new ReentrantLock();
    private final Condition tasksFinished = lifecycleLock.newCondition();
    private final ThreadLocal<Integer> executionDepth = ThreadLocal.withInitial(() -> 0);
    private int runningTasks;

    public PlanFoliaRunnableFactory(JavaPlugin plugin) {
        this.plugin = plugin;
        asyncScheduler = plugin.getServer().getAsyncScheduler();
        globalRegionScheduler = plugin.getServer().getGlobalRegionScheduler();
    }

    @Override
    public UnscheduledTask create(Runnable runnable) {
        return new UnscheduledPlanFoliaTask(
                plugin,
                guardCurrentLifecycle(runnable),
                task -> {},
                asyncScheduler,
                globalRegionScheduler
        );
    }

    @Override
    public UnscheduledTask create(PluginRunnable runnable) {
        return new UnscheduledPlanFoliaTask(
                plugin,
                guardCurrentLifecycle(runnable),
                runnable::setCancellable,
                asyncScheduler,
                globalRegionScheduler
        );
    }

    private Runnable guardCurrentLifecycle(Runnable runnable) {
        long scheduledLifecycle = lifecycle.get();
        return () -> {
            if (!startTask(scheduledLifecycle)) {
                return;
            }
            executionDepth.set(executionDepth.get() + 1);
            try {
                runnable.run();
            } finally {
                int newDepth = executionDepth.get() - 1;
                if (newDepth == 0) {
                    executionDepth.remove();
                } else {
                    executionDepth.set(newDepth);
                }
                finishTask();
            }
        };
    }

    private boolean startTask(long scheduledLifecycle) {
        lifecycleLock.lock();
        try {
            if (scheduledLifecycle != lifecycle.get()) {
                return false;
            }
            runningTasks++;
            return true;
        } finally {
            lifecycleLock.unlock();
        }
    }

    private void finishTask() {
        lifecycleLock.lock();
        try {
            runningTasks--;
            tasksFinished.signalAll();
        } finally {
            lifecycleLock.unlock();
        }
    }

    @Override
    public void cancelAllKnownTasks() {
        lifecycle.incrementAndGet();
        try {
            asyncScheduler.cancelTasks(plugin);
        } finally {
            try {
                globalRegionScheduler.cancelTasks(plugin);
            } finally {
                awaitRunningTasks();
            }
        }
    }

    private void awaitRunningTasks() {
        int tasksOnCurrentThread = executionDepth.get();
        lifecycleLock.lock();
        try {
            while (runningTasks > tasksOnCurrentThread) {
                tasksFinished.awaitUninterruptibly();
            }
        } finally {
            lifecycleLock.unlock();
        }
    }
}
