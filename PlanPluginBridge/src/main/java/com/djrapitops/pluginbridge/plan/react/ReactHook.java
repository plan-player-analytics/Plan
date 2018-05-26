package com.djrapitops.pluginbridge.plan.react;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.plugin.HookHandler;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plugin.api.TimeAmount;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.ITask;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.pluginbridge.plan.Hook;

/**
 * Hook in charge for hooking into React.
 *
 * @author Rsl1122
 */
public class ReactHook extends Hook {

    private static ITask TASK;

    public ReactHook(HookHandler hookHandler) {
        super("com.volmit.react.ReactPlugin", hookHandler);
    }

    private static void setTask(ITask task) {
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
    public void hook() throws NoClassDefFoundError {
        try {
            if (enabled) {
                Plan plan = Plan.getInstance();

                ReactDataTable table = new ReactDataTable((SQLDB) plan.getSystem().getDatabaseSystem().getActiveDatabase());
                table.createTable();
                table.clean();

                ITask task = RunnableFactory.createNew(new DataCollectionTask(table))
                        .runTaskTimerAsynchronously(TimeAmount.SECOND.ticks() * 10L, TimeAmount.SECOND.ticks() * 10L);
                setTask(task);
            }
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }
}