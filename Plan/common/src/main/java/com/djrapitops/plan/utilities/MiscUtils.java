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
package com.djrapitops.plan.utilities;

import java.util.concurrent.TimeUnit;

/**
 * Utility method class containing various static methods.
 *
 * @author AuroraLS3
 */
public class MiscUtils {

    /**
     * Constructor used to hide the public constructor
     */
    private MiscUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void close(AutoCloseable... close) {
        for (AutoCloseable c : close) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignore) {
                    // Closing exceptions are ignored.
                }
            }
        }
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static long fiveMinAgo() {
        return now() - TimeUnit.MINUTES.toMillis(5L);
    }

    public static long dayAgo() {
        return now() - TimeUnit.DAYS.toMillis(1L);
    }

    public static long weekAgo() {
        return now() - (TimeUnit.DAYS.toMillis(7L));
    }

    public static long monthAgo() {
        return now() - (TimeUnit.DAYS.toMillis(30L));
    }
}
