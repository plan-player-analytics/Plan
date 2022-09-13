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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class UnscheduledStandaloneTask implements UnscheduledTask {

    private final ScheduledExecutorService scheduler;
    private final Runnable runnable;
    private final Consumer<Task> cancellableConsumer;

    public UnscheduledStandaloneTask(ScheduledExecutorService scheduler, Runnable runnable, Consumer<Task> cancellableConsumer) {
        this.scheduler = scheduler;
        this.runnable = runnable;
        this.cancellableConsumer = cancellableConsumer;
    }

    @Override
    public Task runTaskAsynchronously() {
        StandaloneTask task = new StandaloneTask(this.scheduler.submit(this.runnable));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTaskLaterAsynchronously(long delayTicks) {
        StandaloneTask task = new StandaloneTask(this.scheduler.schedule(
                this.runnable,
                delayTicks * 50,
                TimeUnit.MILLISECONDS
        ));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTaskTimerAsynchronously(long delayTicks, long periodTicks) {
        StandaloneTask task = new StandaloneTask(this.scheduler.scheduleAtFixedRate(
                runnable,
                delayTicks * 50,
                periodTicks * 50,
                TimeUnit.MILLISECONDS
        ));
        cancellableConsumer.accept(task);
        return task;
    }

    @Override
    public Task runTask() {
        return runTaskAsynchronously();
    }

    @Override
    public Task runTaskLater(long delayTicks) {
        return runTaskLaterAsynchronously(delayTicks);
    }

    @Override
    public Task runTaskTimer(long delayTicks, long periodTicks) {
        return runTaskTimerAsynchronously(delayTicks, periodTicks);
    }
}
