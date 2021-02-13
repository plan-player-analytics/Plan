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

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginRunnable;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import org.mockito.Mockito;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Test implementation of {@link RunnableFactory}.
 * <p>
 * Does not run the {@link AbsRunnable} supplied to it to prevent test collisions
 * from improperly scheduled tasks.
 *
 * @author AuroraLS3
 */
public class TestRunnableFactory extends RunnableFactory {

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
    protected PluginRunnable createNewRunnable(String name, AbsRunnable runnable, long l) {
        PluginRunnable mock = mock(PluginRunnable.class);
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

    private PluginTask run(AbsRunnable runnable) {
        runnable.run();
        return null;
    }

    @Override
    public void cancelAllKnownTasks() {
        /* Nothing to cancel, nothing is actually run. */
    }

}
