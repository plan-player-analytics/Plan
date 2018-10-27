package com.djrapitops.plan.system.tasks;

import com.djrapitops.plan.data.container.TPS;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.djrapitops.plugin.task.AbsRunnable;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for calculating TPS every second.
 *
 * @author Rsl1122
 */
public abstract class TPSCountTimer extends AbsRunnable {

    protected final List<TPS> history;

    protected final Processors processors;
    protected final Processing processing;
    protected final PluginLogger logger;
    protected final ErrorHandler errorHandler;

    private boolean diskErrored = false;

    protected int latestPlayersOnline = 0;

    public TPSCountTimer(
            Processors processors,
            Processing processing,
            PluginLogger logger,
            ErrorHandler errorHandler
    ) {
        this.processors = processors;
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
                processing.submit(processors.tpsInsertProcessor(new ArrayList<>(history)));
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

    protected long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        return (totalMemory - runtime.freeMemory()) / 1000000;
    }

    protected double getCPUUsage() {
        double averageCPUUsage;

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            averageCPUUsage = nativeOsBean.getSystemCpuLoad();
        } else {
            int availableProcessors = osBean.getAvailableProcessors();
            averageCPUUsage = osBean.getSystemLoadAverage() / availableProcessors;
        }
        if (averageCPUUsage < 0) { // If unavailable, getSystemLoadAverage() returns -1
            averageCPUUsage = -1;
        }
        return averageCPUUsage * 100.0;
    }

    protected long getFreeDiskSpace() {
        try {
            File file = new File(new File("").getAbsolutePath());
            return file.getFreeSpace() / 1000000L;
        } catch (SecurityException noPermission) {
            if (!diskErrored) {
                errorHandler.log(L.WARN, this.getClass(), noPermission);
            }
            diskErrored = true;
            return -1;
        }
    }
}
