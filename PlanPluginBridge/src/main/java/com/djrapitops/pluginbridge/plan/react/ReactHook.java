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
package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plugin.task.PluginTask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.pluginbridge.plan.Hook;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Hook in charge for hooking into React.
 *
 * @author Rsl1122
 */
@Singleton
public class ReactHook extends Hook {

    private static PluginTask TASK;

    private final Processing processing;
    private final DBSystem dbSystem;
    private final RunnableFactory runnableFactory;

    @Inject
    public ReactHook(
            Processing processing,
            DBSystem dbSystem,
            RunnableFactory runnableFactory
    ) {
        super("com.volmit.react.ReactPlugin");
        this.processing = processing;
        this.dbSystem = dbSystem;
        this.runnableFactory = runnableFactory;
    }

    private static void setTask(PluginTask task) {
        if (TASK != null) {
            try {
                TASK.cancel();
            } catch (Exception ignored) {
                /* Possible "Task not registered" exception is ignored. */
            }
        }
        ReactHook.TASK = task;
    }

    @Override
    public void hook(HookHandler handler) throws NoClassDefFoundError {
        if (enabled) {
//            ReactDataTable table = new ReactDataTable((SQLDB) dbSystem.getDatabase());
////            try {
////                table.createTable();
////            } catch (DBInitException e) {
////                throw new DBOpException("Failed to create React data table", e);
////            }
////            table.clean();

//            PluginTask task = runnableFactory.create("React Data Task", new ReactDataTask(table, processing))
//                    .runTaskTimerAsynchronously(TimeAmount.toTicks(10L, TimeUnit.SECONDS), TimeAmount.toTicks(10L, TimeUnit.SECONDS));
//            setTask(task);
        }
    }
}