/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.tasks;

import com.djrapitops.plugin.api.systems.TaskCenter;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.IRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import main.java.com.djrapitops.plan.systems.SubSystem;
import main.java.com.djrapitops.plan.systems.Systems;
import main.java.com.djrapitops.plan.utilities.MiscUtils;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public abstract class TaskSystem implements SubSystem {

    protected TPSCountTimer tpsCountTimer;

    public static TaskSystem getInstance() {
        return Systems.getInstance().getTaskSystem();
    }

    protected IRunnable registerTask(AbsRunnable runnable) {
        String taskName = runnable.getName();
        return registerTask(taskName != null ? taskName : runnable.getClass().getSimpleName(), runnable);
    }

    protected IRunnable registerTask(String name, AbsRunnable runnable) {
        return RunnableFactory.createNew(name, runnable);
    }

    @Override
    public void close() {
        TaskCenter.cancelAllKnownTasks(MiscUtils.getIPlan().getClass());
    }

    public TPSCountTimer getTpsCountTimer() {
        return tpsCountTimer;
    }
}