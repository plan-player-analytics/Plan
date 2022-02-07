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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FabricRunnableFactory implements RunnableFactory {

    private ScheduledExecutorService executorService;
    private final Set<FabricTask> tasks;

    public FabricRunnableFactory() {
        this.executorService = Executors.newScheduledThreadPool(8);
        this.tasks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    @Override
    public UnscheduledTask create(Runnable runnable) {
        return new UnscheduledFabricTask(getExecutorService(), runnable, task -> {
        });
    }

    private ScheduledExecutorService getExecutorService() {
        if (executorService.isShutdown() || executorService.isTerminated()) {
            // Hacky way of fixing tasks when plugin is disabled, leaks one thread every reload.
            executorService = Executors.newSingleThreadScheduledExecutor();
        }
        return executorService;
    }

    @Override
    public UnscheduledTask create(PluginRunnable runnable) {
        return new UnscheduledFabricTask(getExecutorService(), runnable, runnable::setCancellable);
    }

    @Override
    public void cancelAllKnownTasks() {
        this.tasks.forEach(Task::cancel);
        this.tasks.clear();
        executorService.shutdown();
    }
}
