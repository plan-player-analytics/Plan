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

import com.djrapitops.plan.settings.config.PlanConfig;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemaphoreAccessCounter {

    private final PlanConfig config;

    private final AtomicInteger accessCounter;
    private final Object lockObject;
    private final Collection<String> holds = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public SemaphoreAccessCounter(PlanConfig config) {
        this.config = config;
        accessCounter = new AtomicInteger(0);
        lockObject = new Object();
    }

    @NotNull
    private static String getAccessingThing() {
        boolean previousWasAccess = false;
        List<StackTraceElement> accessors = new ArrayList<>();
        for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
            if (previousWasAccess) {
                accessors.add(e);
                previousWasAccess = false;
            }
            String call = e.getClassName() + "." + e.getMethodName();
            if ("com.djrapitops.plan.storage.database.SQLDB.query".equals(call)
                    || "com.djrapitops.plan.storage.database.SQLDB.executeTransaction".equals(call)) {
                previousWasAccess = true;
            }
        }
        if (accessors.isEmpty()) accessors.addAll(Arrays.asList(Thread.currentThread().getStackTrace()));
        return accessors.toString();
    }

    public void enter() {
        accessCounter.incrementAndGet();
        holds.add(getAccessingThing());
    }

    public void exit() {
        synchronized (lockObject) {
            holds.remove(getAccessingThing());

            int value = accessCounter.decrementAndGet();
            if (value == 0) {
                lockObject.notifyAll();
            }
        }
    }

    public void waitUntilNothingAccessing() {
        while (accessCounter.get() > 0) {
            synchronized (lockObject) {
                try {
                    logAccess();
                    lockObject.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void logAccess() {
        Logger logger = Logger.getLogger("Plan");
        if (logger == null) logger = Logger.getGlobal();

        if (logger.isLoggable(Level.INFO) && !holds.isEmpty()) {
            logger.log(Level.INFO, "Waiting for these connections to finish:");
            for (String hold : holds) {
                logger.log(Level.INFO, hold);
            }
        }
    }
}
