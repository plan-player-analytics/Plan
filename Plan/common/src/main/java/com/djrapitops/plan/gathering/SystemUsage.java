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
package com.djrapitops.plan.gathering;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

/**
 * Utility class for obtaining System usage statistics.
 *
 * @author AuroraLS3
 */
public class SystemUsage {

    private SystemUsage() {
        /* Static method class */
    }

    /**
     * Check how much memory (in Mb) is in use.
     *
     * @return used memory (megabytes) at the time of fetching
     */
    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        return (totalMemory - runtime.freeMemory()) / 1000000L;
    }

    /**
     * Check how active the system is (CPU) or if not available, using system load average.
     * <p>
     * - On some OSes CPU usage information is not available, and system load average is used instead.
     * - On some OSes system load average is not available.
     *
     * @return 0.0 to 100.0 if CPU, or system load average, or -1 if nothing is available.
     */
    public static double getAverageSystemLoad() {
        double averageUsage;

        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean nativeOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
                // TODO Java 17 related deprecation, use getCpuLoad() after Java 17 swap.
                averageUsage = nativeOsBean.getSystemCpuLoad();
            } else {
                int availableProcessors = osBean.getAvailableProcessors();
                averageUsage = osBean.getSystemLoadAverage() / availableProcessors;
            }
            if (averageUsage < 0) {
                averageUsage = -1; // If unavailable, getSystemLoadAverage() returns -1
            }
        } catch (UnsatisfiedLinkError e) {
            averageUsage = -1; // Using some docker or something
        }
        return averageUsage * 100.0;
    }

    /**
     * Check how much disk space is available on the current partition.
     *
     * @return free disk space (megabytes) on the partition JVM working directory is in.
     * @throws SecurityException if permission is required to see disk space.
     */
    public static long getFreeDiskSpace() {
        File file = new File(new File("").getAbsolutePath());
        return file.getUsableSpace() / 1000000L;
    }

}