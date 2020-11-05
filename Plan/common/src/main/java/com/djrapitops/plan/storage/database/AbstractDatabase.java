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
package com.djrapitops.plan.storage.database;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract class representing a Database.
 * <p>
 * All Operations methods should be only called from an asynchronous thread.
 *
 * @author Rsl1122
 */
public abstract class AbstractDatabase implements Database {

    protected final DBAccessLock accessLock;
    private State state;
    private final AtomicInteger heavyLoadDelayMs = new AtomicInteger(0);

    protected AbstractDatabase() {
        state = State.CLOSED;
        accessLock = new DBAccessLock(this);
    }

    @Override
    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        accessLock.operabilityChanged();
    }

    public boolean isUnderHeavyLoad() {
        return heavyLoadDelayMs.get() != 0;
    }

    public void increaseHeavyLoadDelay() {
        heavyLoadDelayMs.incrementAndGet();
    }

    public void assumeNoMoreHeavyLoad() {
        this.heavyLoadDelayMs.set(0);
    }

    public int getHeavyLoadDelayMs() {
        return heavyLoadDelayMs.get();
    }
}
