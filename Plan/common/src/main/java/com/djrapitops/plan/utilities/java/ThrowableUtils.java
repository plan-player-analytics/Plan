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

import com.djrapitops.plan.exceptions.CallSource;
import org.apache.commons.lang3.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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

    public static void appendEntryPointAsSuppressed(Throwable throwable, StackTraceElement[] originPoint) {
        CallSource source = new CallSource();
        source.setStackTrace(originPoint);
        throwable.addSuppressed(source);
    }

    public static List<StackTraceElement> findCallSites(StackTraceElement[] origin, int howMany, String... calledMethods) {
        int remainingToAdd = 0;
        List<StackTraceElement> accessors = new ArrayList<>();
        Set<String> lookup = new HashSet<>(Arrays.asList(calledMethods));
        for (StackTraceElement e : origin) {
            if (Strings.CI.startsWithAny(e.getModuleName(), "jdk.", "java.")) continue;

            if (remainingToAdd > 0) {
                accessors.add(e);
                remainingToAdd--;
            }
            String call = e.getClassName() + "." + e.getMethodName();
            if (lookup.contains(call)) {
                remainingToAdd = howMany;
            }
        }
        return List.copyOf(accessors);
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

    public static Optional<StackTraceElement> findMethodCall(String methodName) {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (methodName.equals(element.getMethodName())) {
                return Optional.of(element);
            }
        }
        return Optional.empty();
    }
}