/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks.objects;

import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginRunnable;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Fuzzlemann
 * @since 4.5.1
 */
public class TestRunnableFactory extends RunnableFactory {

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Override
    protected PluginRunnable createNewRunnable(String name, AbsRunnable absRunnable, long l) {
        return new PluginRunnable() {
            @Override
            public String getTaskName() {
                return name;
            }

            @Override
            public void cancel() {
                absRunnable.cancel();
            }

            @Override
            public int getTaskId() {
                return absRunnable.getTaskId();
            }

            @Override
            public PluginTask runTask() {
                absRunnable.run();
                return createPluginTask(getTaskId(), true, absRunnable::cancel);
            }

            @Override
            public PluginTask runTaskAsynchronously() {
                executorService.submit(absRunnable);
                return createPluginTask(getTaskId(), false, absRunnable::cancel);
            }

            @Override
            public PluginTask runTaskLater(long l) {
                return runTaskLaterAsynchronously(l);
            }

            @Override
            public PluginTask runTaskLaterAsynchronously(long l) {
                executorService.schedule(absRunnable, TimeAmount.ticksToMillis(l), TimeUnit.MILLISECONDS);
                return createPluginTask(getTaskId(), false, absRunnable::cancel);
            }

            @Override
            public PluginTask runTaskTimer(long l, long l1) {
                return runTaskLaterAsynchronously(l);
            }

            @Override
            public PluginTask runTaskTimerAsynchronously(long l, long l1) {
                executorService.scheduleAtFixedRate(absRunnable, TimeAmount.ticksToMillis(l), TimeAmount.ticksToMillis(l1), TimeUnit.MILLISECONDS);
                return createPluginTask(getTaskId(), false, absRunnable::cancel);
            }

            @Override
            public long getTime() {
                return l;
            }
        };
    }

    @Override
    public void cancelAllKnownTasks() {
        executorService.shutdownNow();
    }

    private PluginTask createPluginTask(int taskID, boolean sync, ICloseTask closeTask) {
        return new PluginTask() {
            @Override
            public int getTaskId() {
                return taskID;
            }

            @Override
            public boolean isSync() {
                return sync;
            }

            @Override
            public void cancel() {
                if (closeTask != null) {
                    closeTask.close();
                }
            }
        };
    }

    private interface ICloseTask {
        void close();
    }
}
