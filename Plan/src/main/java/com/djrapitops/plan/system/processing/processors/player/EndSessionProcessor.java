/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
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
