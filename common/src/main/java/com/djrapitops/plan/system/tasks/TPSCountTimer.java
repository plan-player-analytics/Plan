package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.TPSInsertProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public abstract class TPSCountTimer<T extends PlanPlugin> extends AbsRunnable {

    protected final T plugin;
    protected final List<TPS> history;

    protected int latestPlayersOnline = 0;

    public TPSCountTimer(T plugin) {
        super("TPSCountTimer");
        this.plugin = plugin;
        history = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            long nanoTime = System.nanoTime();
            long now = System.currentTimeMillis();

            addNewTPSEntry(nanoTime, now);

            if (history.size() >= 60) {
                Processing.submit(new TPSInsertProcessor(new ArrayList<>(history)));
                history.clear();
            }
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            Log.error("TPS Count Task Disabled due to error, reload Plan to re-enable.");
            Log.toLog(this.getClass(), e);
            cancel();
        }
    }

    public abstract void addNewTPSEntry(long nanoTime, long now);

    public int getLatestPlayersOnline() {
        return latestPlayersOnline;
    }
}
