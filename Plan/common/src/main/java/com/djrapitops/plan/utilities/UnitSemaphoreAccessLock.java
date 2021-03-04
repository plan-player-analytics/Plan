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

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Synchronizes a critical section of code so that only a single thread can access it at a time.
 *
 * @author AuroraLS3
 */
public class UnitSemaphoreAccessLock {

    private final AtomicBoolean accessing;
    private final Object lockObject;

    public UnitSemaphoreAccessLock() {
        accessing = new AtomicBoolean(false);
        lockObject = new Object();
    }

    public void enter() {
        try {
            synchronized (lockObject) {
                while (accessing.get()) {
                    lockObject.wait();
                }
                accessing.set(true);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void exit() {
        synchronized (lockObject) {
            accessing.set(false);
            lockObject.notify();
        }
    }
}
