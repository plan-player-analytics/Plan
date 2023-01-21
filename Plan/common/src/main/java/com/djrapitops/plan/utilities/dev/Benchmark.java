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
package com.djrapitops.plan.utilities.dev;

import com.djrapitops.plan.delivery.formatting.Formatter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author AuroraLS3
 */
public class Benchmark {

    private static final Formatter<Long> FORMATTER = new BenchmarkFormatter();

    private Benchmark() {
        /* Only used for in-development benchmarks. (static methods) */
    }

    public static void bench(Runnable runnable) {
        long start = System.nanoTime();
        try {
            runnable.run();
        } finally {
            long end = System.nanoTime();
            long durationNanos = end - start;

            printResult(durationNanos);
        }
    }

    public static <T> T bench(Supplier<T> supplier) {
        long start = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            long end = System.nanoTime();
            long durationNanos = end - start;

            printResult(durationNanos);
        }
    }

    private static void printResult(long durationNanos) {
        String timeTaken = FORMATTER.apply(durationNanos);

        StackTraceElement caller = Thread.currentThread().getStackTrace()[3];
        String calledFrom = caller.getClassName() + "#" + caller.getMethodName() + ":" + caller.getLineNumber();

        StringBuilder builder = new StringBuilder(timeTaken);
        while (builder.length() < 16) {
            builder.append(' ');
        }
        builder.append(" - ").append(calledFrom);

        Logger.getLogger("Plan").log(Level.INFO, builder::toString);
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public static @interface Slow {
        String value(); // How long the benchmark took when done (look at git blame for date)
    }

    private static class BenchmarkFormatter implements Formatter<Long> {
        @Override
        public String apply(Long benchLengthNanos) {
            long nanos = benchLengthNanos % TimeUnit.MILLISECONDS.toNanos(1L);
            long millis = TimeUnit.NANOSECONDS.toMillis(benchLengthNanos) % TimeUnit.SECONDS.toMillis(1L);
            long seconds = TimeUnit.NANOSECONDS.toSeconds(benchLengthNanos);

            String subSeconds = millis <= 0 ? "woah (" + nanos + "ns)" : "fast (" + millis + "ms)";
            return seconds <= 0 ? subSeconds : "slow (" + seconds + "s)";
        }
    }

}
