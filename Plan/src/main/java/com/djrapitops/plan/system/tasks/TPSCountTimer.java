package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.processing.processors.TPSInsertProcessor;
import com.djrapitops.plan.utilities.MiscUtils;
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
        long nanoTime = System.nanoTime();
        long now = MiscUtils.getTime();

        addNewTPSEntry(nanoTime, now);

        if (history.size() >= 60) {
            new TPSInsertProcessor(new ArrayList<>(history)).queue();
            history.clear();
        }
    }

    public abstract void addNewTPSEntry(long nanoTime, long now);

    public int getLatestPlayersOnline() {
        return latestPlayersOnline;
    }
}
