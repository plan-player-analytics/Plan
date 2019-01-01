/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package utilities.mocks.objects;

import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.PluginRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

import static org.mockito.Mockito.mock;

/**
 * Test implementation of {@link RunnableFactory}.
 * <p>
 * Does not run the {@link AbsRunnable} supplied to it to prevent test collisions
 * from improperly scheduled tasks.
 *
 * @author Rsl1122
 */
public class TestRunnableFactory extends RunnableFactory {

    @Override
    protected PluginRunnable createNewRunnable(String name, AbsRunnable absRunnable, long l) {
        return mock(PluginRunnable.class);
    }

    @Override
    public void cancelAllKnownTasks() {
        /* Nothing to cancel, nothing is actually run. */
    }
}
