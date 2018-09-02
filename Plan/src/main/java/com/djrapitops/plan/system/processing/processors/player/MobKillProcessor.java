package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.CriticalRunnable;

import java.util.Optional;
import java.util.UUID;

/**
 * Processor Class for KillEvent information when the killer is a
 * player.
 * <p>
 * Adds PlayerKill or a Mob kill to the active Session.
 *
 * @author Rsl1122
 * @since 4.0.0
 */
public class MobKillProcessor implements CriticalRunnable {

    private final UUID uuid;

    /**
     * Constructor.
     *
     * @param uuid       UUID of the killer.
     */
    public MobKillProcessor(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        if (!cachedSession.isPresent()) {
            return;
        }
        Session session = cachedSession.get();

        session.mobKilled();
    }
}
