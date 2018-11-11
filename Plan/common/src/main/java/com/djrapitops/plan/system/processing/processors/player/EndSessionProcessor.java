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
package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.UUID;

/**
 * Ends a session and saves it to the database.
 *
 * @author Rsl1122
 */
public class EndSessionProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final long time;

    private final SessionCache sessionCache;

    EndSessionProcessor(UUID uuid, long time, SessionCache sessionCache) {
        this.uuid = uuid;
        this.time = time;
        this.sessionCache = sessionCache;
    }

    @Override
    public void run() {
        sessionCache.endSession(uuid, time);
    }
}
