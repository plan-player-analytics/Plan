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
package utilities.mocks.objects;

import net.playeranalytics.plugin.scheduling.PluginRunnable;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.Task;
import net.playeranalytics.plugin.scheduling.UnscheduledTask;
import org.mockito.Mockito;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Test implementation of {@link RunnableFactory}.
 *
 * @author AuroraLS3
 */
public class TestRunnableFactory implements RunnableFactory {

    private final boolean callOnSameThread;

    public TestRunnableFactory() {
        this(false);
    }

    private TestRunnableFactory(boolean callOnSameThread) {
        this.callOnSameThread = callOnSameThread;
    }

    public static RunnableFactory forSameThread() {
        return new TestRunnableFactory(true);
    }

    @Override
    public UnscheduledTask create(Runnable runnable) {
        return create(new PluginRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        });
    }

    @Override
    public UnscheduledTask create(PluginRunnable runnable) {
        UnscheduledTask mock = mock(UnscheduledTask.class);
        if (callOnSameThread) {
            lenient().when(mock.runTask()).then(invocation -> run(runnable));
            lenient().when(mock.runTaskAsynchronously()).then(invocation -> run(runnable));
            lenient().when(mock.runTaskLater(Mockito.anyLong())).then(invocation -> run(runnable));
            lenient().when(mock.runTaskLaterAsynchronously(Mockito.anyLong())).then(invocation -> run(runnable));
            lenient().when(mock.runTaskTimer(Mockito.anyLong(), Mockito.anyLong())).then(invocation -> run(runnable));
            lenient().when(mock.runTaskTimerAsynchronously(Mockito.anyLong(), Mockito.anyLong())).then(invocation -> run(runnable));
        }
        return mock;
    }

    private Task run(PluginRunnable runnable) {
        runnable.run();
        return Mockito.mock(Task.class);
    }

    @Override
    public void cancelAllKnownTasks() {
        /* Nothing to cancel, nothing is actually run. */
    }

}
