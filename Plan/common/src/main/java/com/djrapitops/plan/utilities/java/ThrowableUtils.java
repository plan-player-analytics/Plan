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
package com.djrapitops.plan.utilities.java;

import com.djrapitops.plugin.utilities.ArrayUtil;

/**
 * Utilities for manipulating different Throwables.
 *
 * @author Rsl1122
 */
public class ThrowableUtils {

    private ThrowableUtils() {
        /* Static method class */
    }

    public static void appendEntryPointToCause(Throwable throwable, Throwable originPoint) {
        Throwable cause = throwable.getCause();
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        cause.setStackTrace(ArrayUtil.merge(cause.getStackTrace(), originPoint.getStackTrace()));
    }

    public static String findCallerAfterClass(StackTraceElement[] stackTrace, Class<?> afterThis) {
        boolean found = false;
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (found) {
                return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
            }
            if (stackTraceElement.getClassName().contains(afterThis.getName())) {
                found = true;
            }
        }
        return "Unknown";
    }

}