package com.djrapitops.plan.system.afk;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Keeps track how long player has been afk during a session
 *
 * @author Rsl1122
 */
public class AFKTracker {

    private final Map<UUID, Long> lastMovement;
    private final long afkThresholdMs;

    public AFKTracker() {
        lastMovement = new HashMap<>();
        afkThresholdMs = Settings.AFK_THRESHOLD_MINUTES.getNumber() * TimeAmount.MINUTE.ms();
    }

    public void performedAction(UUID uuid, long time) {
        Long lastMoved = lastMovement.getOrDefault(uuid, time);
        lastMovement.put(uuid, lastMoved);

        if (time - lastMoved < afkThresholdMs) {
            // Threshold not crossed, no action required.
            return;
        }

        long timeAFK = time - lastMoved;

        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        if (!cachedSession.isPresent()) {
            return;
        }
        Session session = cachedSession.get();
        session.addAFKTime(timeAFK);
    }

    public void loggedOut(UUID uuid, long time) {
        performedAction(uuid, time);
        lastMovement.remove(uuid);
    }
}