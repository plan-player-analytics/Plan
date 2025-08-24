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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Utilities for manipulating different Throwable stack traces.
 *
 * @author AuroraLS3
 */
public class ThrowableUtils {

    private ThrowableUtils() {
        /* Static method class */
    }

    public static void appendEntryPointToCause(Throwable throwable, StackTraceElement[] originPoint) {
        Throwable cause = throwable.getCause();
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        cause.setStackTrace(
                combineStackTrace(originPoint, cause.getStackTrace())
        );
    }

    @NotNull
    public static StackTraceElement[] combineStackTrace(StackTraceElement[] originPoint, StackTraceElement[] cause) {
        if (originPoint == null && cause == null) return new StackTraceElement[0];
        if (originPoint == null) return cause;
        if (cause == null) return originPoint;

        return Stream.concat(
                Arrays.stream(cause),
                Arrays.stream(originPoint)
        ).toArray(StackTraceElement[]::new);
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