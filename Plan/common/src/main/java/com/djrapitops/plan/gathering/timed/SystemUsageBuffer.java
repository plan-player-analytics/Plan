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
package com.djrapitops.plan.gathering.timed;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.gathering.SystemUsage;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;
import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.scheduling.TimeAmount;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.TimeUnit;

/**
 * Task for performing system resource usage checks asynchronously
 *
 * @author AuroraLS3
 */
@Singleton
public class SystemUsageBuffer {

    private double cpu = -1.0;
    private long ram = -1L;
    private long freeDiskSpace = -1L;

    @Inject
    public SystemUsageBuffer() {
        warmUp();
    }

    public void warmUp() {
        SystemUsage.getAverageSystemLoad();
        SystemUsage.getUsedMemory();
        SystemUsage.getFreeDiskSpace();
    }

    public double getCpu() {
        return cpu;
    }

    public long getRam() {
        return ram;
    }

    public long getFreeDiskSpace() {
        return freeDiskSpace;
    }

    @Singleton
    public static class RamAndCpuTask extends TaskSystem.Task {
        private final SystemUsageBuffer buffer;
        private final PluginLogger logger;

        @Inject
        public RamAndCpuTask(SystemUsageBuffer buffer, PluginLogger logger) {
            this.buffer = buffer;
            this.logger = logger;
        }

        @Override
        public void run() {
            try {
                buffer.cpu = SystemUsage.getAverageSystemLoad();
                buffer.ram = SystemUsage.getUsedMemory();
            } catch (Exception e) {
                logger.error("RAM & CPU sampling task had to be stopped due to error: " + e.toString());
                cancel();
            }
        }

        @Override
        public void register(RunnableFactory runnableFactory) {
            long delay = TimeAmount.toTicks(1, TimeUnit.MINUTES) - TimeAmount.toTicks(500, TimeUnit.MILLISECONDS);
            long period = TimeAmount.toTicks(1, TimeUnit.SECONDS);
            runnableFactory.create(this).runTaskTimerAsynchronously(delay, period);
        }
    }

    @Singleton
    public static class DiskTask extends TaskSystem.Task {
        private final PlanConfig config;
        private final SystemUsageBuffer buffer;
        private final PluginLogger logger;
        private final ErrorLogger errorLogger;

        private Boolean gatherDisk = null;
        private boolean diskErrored = false;

        @Inject
        public DiskTask(PlanConfig config, SystemUsageBuffer buffer, PluginLogger logger, ErrorLogger errorLogger) {
            this.config = config;
            this.buffer = buffer;
            this.logger = logger;
            this.errorLogger = errorLogger;
        }

        @Override
        public void run() {
            if (gatherDisk == null) gatherDisk = config.get(DataGatheringSettings.DISK_SPACE);
            if (Boolean.FALSE.equals(gatherDisk)) return;
            try {
                buffer.freeDiskSpace = SystemUsage.getFreeDiskSpace();
            } catch (SecurityException noPermission) {
                if (!diskErrored) {
                    errorLogger.warn(noPermission, ErrorContext.builder()
                            .whatToDo("Resolve " + noPermission.getMessage() + " via OS or JVM permissions").build());
                }
                diskErrored = true;
            } catch (Exception e) {
                logger.error("Free Disk sampling task had to be stopped due to error: " + e.toString());
                cancel();
            }
        }

        @Override
        public void register(RunnableFactory runnableFactory) {
            long delay = TimeAmount.toTicks(50, TimeUnit.SECONDS);
            long period = TimeAmount.toTicks(1, TimeUnit.SECONDS);
            runnableFactory.create(this).runTaskTimerAsynchronously(delay, period);
        }
    }

}
