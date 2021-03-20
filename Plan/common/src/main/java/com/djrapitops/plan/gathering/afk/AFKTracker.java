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
package com.djrapitops.plan.gathering.afk;

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;

import java.util.*;

/**
 * Keeps track how long player has been afk during a session
 *
 * @author AuroraLS3
 */
public class AFKTracker {

    private final Set<UUID> usedAFKCommand;
    private final Map<UUID, Long> lastMovement;
    private final PlanConfig config;
    private Long afkThresholdMs;

    public AFKTracker(PlanConfig config) {
        this.config = config;
        usedAFKCommand = new HashSet<>();
        lastMovement = new HashMap<>();
    }

    public long getAfkThreshold() {
        if (afkThresholdMs == null) {
            afkThresholdMs = config.get(TimeSettings.AFK_THRESHOLD);
        }
        return afkThresholdMs;
    }

    public void hasIgnorePermission(UUID uuid) {
        lastMovement.put(uuid, -1L);
    }

    public void usedAfkCommand(UUID uuid, long time) {
        Long lastMoved = lastMovement.getOrDefault(uuid, time);
        if (lastMoved == -1) {
            return;
        }
        usedAFKCommand.add(uuid);
        lastMovement.put(uuid, time - getAfkThreshold());
    }

    public long performedAction(UUID uuid, long time) {
        Long lastMoved = lastMovement.getOrDefault(uuid, time);
        // Ignore afk permission
        if (lastMoved == -1) {
            return 0L;
        }
        lastMovement.put(uuid, time);

        try {
            if (time - lastMoved < getAfkThreshold()) {
                // Threshold not crossed, no action required.
                return 0L;
            }

            long removeAfkCommandEffect = usedAFKCommand.contains(uuid) ? getAfkThreshold() : 0;
            long timeAFK = time - lastMoved - removeAfkCommandEffect;

            SessionCache.getCachedSession(uuid)
                    .ifPresent(session -> session.addAfkTime(timeAFK));
            return timeAFK;
        } finally {
            usedAFKCommand.remove(uuid);
        }
    }

    public long loggedOut(UUID uuid, long time) {
        long timeAFK = performedAction(uuid, time);
        lastMovement.remove(uuid);
        usedAFKCommand.remove(uuid);
        return timeAFK;
    }

    public boolean isAfk(UUID uuid) {
        long time = System.currentTimeMillis();

        Long lastMoved = lastMovement.get(uuid);
        if (lastMoved == null || lastMoved == -1) {
            return false;
        }
        return time - lastMoved > getAfkThreshold();
    }
}