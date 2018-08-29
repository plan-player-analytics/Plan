package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.TPSInsertProcessor;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public abstract class TPSCountTimer extends AbsRunnable {

    protected final List<TPS> history;

    protected final Processing processing;
    protected final PluginLogger logger;
    protected final ErrorHandler errorHandler;

    protected int latestPlayersOnline = 0;

    public TPSCountTimer(Processing processing, PluginLogger logger, ErrorHandler errorHandler) {
        this.processing = processing;
        this.logger = logger;
        this.errorHandler = errorHandler;
        history = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            long nanoTime = System.nanoTime();
            long now = System.currentTimeMillis();

            addNewTPSEntry(nanoTime, now);

            if (history.size() >= 60) {
                processing.submit(new TPSInsertProcessor(new ArrayList<>(history)));
                history.clear();
            }
        } catch (Exception | NoClassDefFoundError | NoSuchMethodError | NoSuchFieldError e) {
            logger.error("TPS Count Task Disabled due to error, reload Plan to re-enable.");
            errorHandler.log(L.ERROR, this.getClass(), e);
            cancel();
        }
    }

    public abstract void addNewTPSEntry(long nanoTime, long now);

    public int getLatestPlayersOnline() {
        return latestPlayersOnline;
    }
}
