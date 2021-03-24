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

import java.util.concurrent.atomic.AtomicInteger;

public class SemaphoreAccessCounter {

    private final AtomicInteger accessCounter;
    private final Object lockObject;

    public SemaphoreAccessCounter() {
        accessCounter = new AtomicInteger(0);
        lockObject = new Object();
    }

    public void enter() {
        accessCounter.incrementAndGet();
    }

    public void exit() {
        synchronized (lockObject) {
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
                    lockObject.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
