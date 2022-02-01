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
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.TimeSettings;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Keeps track how long player has been afk during a session
 *
 * @author AuroraLS3
 */
public class AFKTracker {

    public static final long IGNORES_AFK = -1L;

    private final Set<UUID> usedAFKCommand;
    private final PlanConfig config;
    private Long afkThresholdMs;

    public AFKTracker(PlanConfig config) {
        this.config = config;
        usedAFKCommand = new HashSet<>();
    }

    public long getAfkThreshold() {
        if (afkThresholdMs == null) {
            afkThresholdMs = config.get(TimeSettings.AFK_THRESHOLD);
        }
        return afkThresholdMs;
    }

    public void hasIgnorePermission(UUID playerUUID) {
        storeLastMovement(playerUUID, IGNORES_AFK);
    }

    private void storeLastMovement(UUID playerUUID, long time) {
        SessionCache.getCachedSession(playerUUID)
                .ifPresent(activeSession -> activeSession.setLastMovementForAfkCalculation(time));
    }

    private long getLastMovement(UUID playerUUID, long time) {
        return getLastMovement(playerUUID)
                .orElse(time);
    }

    private Optional<Long> getLastMovement(UUID playerUUID) {
        return SessionCache.getCachedSession(playerUUID)
                .map(ActiveSession::getLastMovementForAfkCalculation);
    }

    public void usedAfkCommand(UUID playerUUID, long time) {
        long lastMoved = getLastMovement(playerUUID, time);
        if (lastMoved == IGNORES_AFK) {
            return;
        }
        usedAFKCommand.add(playerUUID);
        storeLastMovement(playerUUID, time - getAfkThreshold());
    }

    public long performedAction(UUID playerUUID, long time) {
        long lastMoved = getLastMovement(playerUUID, time);
        // Ignore afk permission
        if (lastMoved == IGNORES_AFK) {
            return 0L;
        }
        storeLastMovement(playerUUID, time);

        try {
            if (time - lastMoved < getAfkThreshold()) {
                // Threshold not crossed, no action required.
                return 0L;
            }

            long removeAfkCommandEffect = usedAFKCommand.contains(playerUUID) ? getAfkThreshold() : 0;
            long timeAFK = time - lastMoved - removeAfkCommandEffect;

            SessionCache.getCachedSession(playerUUID)
                    .ifPresent(session -> session.addAfkTime(timeAFK));
            return timeAFK;
        } finally {
            usedAFKCommand.remove(playerUUID);
        }
    }

    public long loggedOut(UUID uuid, long time) {
        long timeAFK = performedAction(uuid, time);
        usedAFKCommand.remove(uuid);
        return timeAFK;
    }

    public boolean isAfk(UUID playerUUID) {
        long time = System.currentTimeMillis();

        Optional<Long> lastMoved = getLastMovement(playerUUID);
        if (!lastMoved.isPresent() || lastMoved.get() == IGNORES_AFK) {
            return false;
        }
        return time - lastMoved.get() > getAfkThreshold();
    }
}