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
 * @since 4.3.0
 */
public class SpongeKillProcessor implements CriticalRunnable {

    private final UUID uuid;
    private final UUID deadUUID;
    private final String weaponName;
    private final long time;

    /**
     * Constructor.
     *
     * @param uuid       UUID of the killer.
     * @param time       Epoch ms the event occurred.
     * @param deadUUID   Dead entity (Mob or Player)
     * @param weaponName Weapon used.
     */
    public SpongeKillProcessor(UUID uuid, long time, UUID deadUUID, String weaponName) {
        this.uuid = uuid;
        this.time = time;
        this.deadUUID = deadUUID;
        this.weaponName = weaponName;
    }

    @Override
    public void run() {
        Optional<Session> cachedSession = SessionCache.getCachedSession(uuid);
        if (!cachedSession.isPresent()) {
            return;
        }
        Session session = cachedSession.get();

        if (deadUUID != null) {
            session.playerKilled(new PlayerKill(deadUUID, weaponName, time));
        } else {
            session.mobKilled();
        }
    }
}
