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

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * TaskSystem that registers tasks that were previously registered inside Plugin classes.
 *
 * Subclasses register actual tasks.
 *
 * @author Rsl1122
 */
public abstract class TaskSystem implements SubSystem {

    protected final RunnableFactory runnableFactory;

    protected TaskSystem(RunnableFactory runnableFactory) {
        this.runnableFactory = runnableFactory;
    }

    protected PluginRunnable registerTask(AbsRunnable runnable) {
        String taskName = runnable.getClass().getSimpleName();
        return registerTask(taskName, runnable);
    }

    public PluginRunnable registerTask(String name, AbsRunnable runnable) {
        return runnableFactory.create(name, runnable);
    }

    @Override
    public void disable() {
        runnableFactory.cancelAllKnownTasks();
    }

}
