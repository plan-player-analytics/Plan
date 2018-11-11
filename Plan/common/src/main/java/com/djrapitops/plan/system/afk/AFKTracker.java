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
package com.djrapitops.plan.system.afk;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plan.system.settings.config.PlanConfig;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Keeps track how long player has been afk during a session
 *
 * @author Rsl1122
 */
public class AFKTracker {

    private final Set<UUID> usedAFKCommand;
    private final Map<UUID, Long> lastMovement;
    private final long afkThresholdMs;

    public AFKTracker(PlanConfig config) {
        usedAFKCommand = new HashSet<>();
        lastMovement = new HashMap<>();
        afkThresholdMs = TimeUnit.MINUTES.toMillis(config.getNumber(Settings.AFK_THRESHOLD_MINUTES));
    }

    public void hasIgnorePermission(UUID uuid) {
        lastMovement.put(uuid, -1L);
    }

    public void usedAfkCommand(UUID uuid, long time) {
        usedAFKCommand.add(uuid);
        lastMovement.put(uuid, time - afkThresholdMs);
    }

    public void performedAction(UUID uuid, long time) {
        Long lastMoved = lastMovement.getOrDefault(uuid, time);
        if (lastMoved == -1) {
            return;
        }
        lastMovement.put(uuid, time);

        try {
            if (time - lastMoved < afkThresholdMs) {
                // Threshold not crossed, no action required.
                return;
            }

            long removeAfkCommandEffect = usedAFKCommand.contains(uuid) ? afkThresholdMs : 0;
            long timeAFK = time - lastMoved - removeAfkCommandEffect;

            Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
            if (!cachedSession.isPresent()) {
                return;
            }
            Session session = cachedSession.get();
            session.addAFKTime(timeAFK);
        } finally {
            usedAFKCommand.remove(uuid);
        }
    }

    public void loggedOut(UUID uuid, long time) {
        performedAction(uuid, time);
        lastMovement.remove(uuid);
        usedAFKCommand.remove(uuid);
    }

    public boolean isAfk(UUID uuid) {
        long time = System.currentTimeMillis();

        Long lastMoved = lastMovement.get(uuid);
        if (lastMoved == null || lastMoved == -1) {
            return false;
        }
        return time - lastMoved > afkThresholdMs;
    }
}