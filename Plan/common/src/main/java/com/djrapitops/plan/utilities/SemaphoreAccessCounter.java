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
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SemaphoreAccessCounter {

    private final PlanConfig config;

    private final AtomicInteger accessCounter;
    private final Object lockObject;
    private final Collection<String> holds = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private AtomicBoolean logHolds = null;

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
        return accessors.toString();
    }

    public void enter() {
        accessCounter.incrementAndGet();

        if (logHolds == null) logHolds = new AtomicBoolean(config.isTrue(PluginSettings.DEV_MODE));
        if (logHolds.get()) holds.add(getAccessingThing());
    }

    public void exit() {
        synchronized (lockObject) {
            if (logHolds == null) logHolds = new AtomicBoolean(config.isTrue(PluginSettings.DEV_MODE));
            if (logHolds.get()) holds.remove(getAccessingThing());

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
        if (logHolds != null && logHolds.get()) {
            Logger logger = Logger.getLogger("Plan");
            if (logger == null) logger = Logger.getGlobal();

            if (logger.isLoggable(Level.INFO) && !holds.isEmpty()) {
                logger.log(Level.INFO, "DEBUG - Following call sites are holding connection:");
                for (String hold : holds) {
                    logger.log(Level.INFO, String.format("DEBUG - %s", hold));
                }
            }
        }
    }
}
