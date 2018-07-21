package com.djrapitops.plan.system.afk;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.settings.Settings;
import com.djrapitops.plugin.api.TimeAmount;

import java.util.*;

/**
 * Keeps track how long player has been afk during a session
 *
 * @author Rsl1122
 */
public class AFKTracker {

    private final Set<UUID> usedAFKCommand;
    private final Map<UUID, Long> lastMovement;
    private final long afkThresholdMs;

    public AFKTracker() {
        usedAFKCommand = new HashSet<>();
        lastMovement = new HashMap<>();
        afkThresholdMs = Settings.AFK_THRESHOLD_MINUTES.getNumber() * TimeAmount.MINUTE.ms();
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
}