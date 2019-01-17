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
