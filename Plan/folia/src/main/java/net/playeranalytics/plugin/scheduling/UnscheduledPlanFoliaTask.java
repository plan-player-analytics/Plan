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

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A task that has not yet been submitted to a Folia scheduler.
 */
public class UnscheduledPlanFoliaTask implements UnscheduledTask {

    private final JavaPlugin plugin;
    private final Runnable runnable;
    private final Consumer<Task> cancellableConsumer;
    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalRegionScheduler;

    public UnscheduledPlanFoliaTask(
            JavaPlugin plugin,
            Runnable runnable,
            Consumer<Task> cancellableConsumer,
            AsyncScheduler asyncScheduler,
            GlobalRegionScheduler globalRegionScheduler
    ) {
        this.plugin = plugin;
        this.runnable = runnable;
        this.cancellableConsumer = cancellableConsumer;
        this.asyncScheduler = asyncScheduler;
        this.globalRegionScheduler = globalRegionScheduler;
    }

    @Override
    public Task runTaskAsynchronously() {
        return makeCancellable(new FoliaTask(asyncScheduler.runNow(plugin, scheduled -> runnable.run()), true));
    }

    @Override
    public Task runTaskLaterAsynchronously(long delayTicks) {
        FoliaTask task = new FoliaTask(asyncScheduler.runDelayed(
                plugin,
                scheduled -> runnable.run(),
                TimeAmount.ticksToMillis(delayTicks),
                TimeUnit.MILLISECONDS
        ), true);
        return makeCancellable(task);
    }

    @Override
    public Task runTaskTimerAsynchronously(long delayTicks, long periodTicks) {
        FoliaTask task = new FoliaTask(asyncScheduler.runAtFixedRate(
                plugin,
                scheduled -> runnable.run(),
                TimeAmount.ticksToMillis(delayTicks),
                TimeAmount.ticksToMillis(periodTicks),
                TimeUnit.MILLISECONDS
        ), true);
        return makeCancellable(task);
    }

    @Override
    public Task runTask() {
        return makeCancellable(new FoliaTask(globalRegionScheduler.run(plugin, scheduled -> runnable.run()), false));
    }

    @Override
    public Task runTaskLater(long delayTicks) {
        FoliaTask task = new FoliaTask(globalRegionScheduler.runDelayed(
                plugin,
                scheduled -> runnable.run(),
                delayTicks
        ), false);
        return makeCancellable(task);
    }

    @Override
    public Task runTaskTimer(long delayTicks, long periodTicks) {
        FoliaTask task = new FoliaTask(globalRegionScheduler.runAtFixedRate(
                plugin,
                scheduled -> runnable.run(),
                delayTicks,
                periodTicks
        ), false);
        return makeCancellable(task);
    }

    private Task makeCancellable(Task task) {
        cancellableConsumer.accept(task);
        return task;
    }
}
