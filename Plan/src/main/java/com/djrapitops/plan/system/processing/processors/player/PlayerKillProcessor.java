package com.djrapitops.plan.system.processing.processors.player;

import com.djrapitops.plan.data.container.PlayerKill;
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
public class PlayerKillProcessor implements CriticalRunnable {

    private final UUID killer;
    private final UUID victim;
    private final String weaponName;
    private final long time;

    /**
     * Constructor.
     *
     * @param killer       UUID of the killer.
     * @param time       Epoch ms the event occurred.
     * @param victim       Dead entity (Mob or Player)
     * @param weaponName Weapon used.
     */
    public PlayerKillProcessor(UUID killer, long time, UUID victim, String weaponName) {
        this.killer = killer;
        this.time = time;
        this.victim = victim;
        this.weaponName = weaponName;
    }

    @Override
    public void run() {
        Optional<Session> cachedSession = SessionCache.getCachedSession(killer);
        if (!cachedSession.isPresent()) {
            return;
        }
        Session session = cachedSession.get();

        session.playerKilled(new PlayerKill(victim, weaponName, time));
    }
}
