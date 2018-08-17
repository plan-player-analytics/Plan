/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.system.SubSystem;
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

    protected final PlanPlugin plugin;
    protected TPSCountTimer tpsCountTimer;
    protected final RunnableFactory runnableFactory;

    public TaskSystem(PlanPlugin plugin, TPSCountTimer tpsCountTimer) {
        this.plugin = plugin;
        this.tpsCountTimer = tpsCountTimer;
        runnableFactory = plugin.getRunnableFactory();
    }

    protected PluginRunnable registerTask(AbsRunnable runnable) {
        String taskName = runnable.getClass().getSimpleName();
        return registerTask(taskName, runnable);
    }

    protected PluginRunnable registerTask(String name, AbsRunnable runnable) {
        return runnableFactory.create(name, runnable);
    }

    @Override
    public void disable() {
        runnableFactory.cancelAllKnownTasks();
    }

}
