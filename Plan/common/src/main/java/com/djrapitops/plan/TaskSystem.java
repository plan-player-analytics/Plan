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
package com.djrapitops.plan;

import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.Set;

/**
 * TaskSystem that registers tasks for the plugin.
 * See platform specific [Platform]TaskModule classes for what Tasks are registered.
 *
 * @author AuroraLS3
 */
@Singleton
public class TaskSystem implements SubSystem {

    private final RunnableFactory runnableFactory;
    private final Set<Task> tasks;

    @Inject
    public TaskSystem(
            RunnableFactory runnableFactory,
            Set<Task> tasks
    ) {
        this.runnableFactory = runnableFactory;
        this.tasks = tasks;
    }

    @Override
    public void enable() {
        for (Task task : tasks) task.register(runnableFactory);
    }

    @Override
    public void disable() {
        runnableFactory.cancelAllKnownTasks();
    }

    public <T extends Task> Optional<T> getTask(Class<T> ofType) {
        for (Task task : tasks) {
            if (task.getClass().equals(ofType)) return Optional.of(ofType.cast(task));
        }
        return Optional.empty();
    }

    public abstract static class Task extends PluginRunnable {
        public abstract void register(RunnableFactory runnableFactory);
    }

}
