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

import java.util.function.Supplier;

public class ThreadContextClassLoaderSwap {

    private ThreadContextClassLoaderSwap() {
        /* static method utility class */
    }

    public static void performOperation(ClassLoader usingClassLoader, Runnable operation) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Jetty uses Thread context classloader, so we need to change to plugin classloader where the ALPNProcessor is.
            Thread.currentThread().setContextClassLoader(usingClassLoader);

            operation.run();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    public static <T> T performOperation(ClassLoader usingClassLoader, Supplier<T> operation) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // Jetty uses Thread context classloader, so we need to change to plugin classloader where the ALPNProcessor is.
            Thread.currentThread().setContextClassLoader(usingClassLoader);

            return operation.get();
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

}
