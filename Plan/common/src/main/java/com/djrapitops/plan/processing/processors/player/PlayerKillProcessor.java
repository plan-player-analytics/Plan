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
package com.djrapitops.plan.processing.processors.player;

import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.gathering.domain.Session;
import com.djrapitops.plan.processing.CriticalRunnable;

import java.util.Optional;
import java.util.UUID;

/**
 * Processor Class for KillEvent information when the killer is a
 * player.
 * <p>
 * Adds PlayerKill or a Mob kill to the active Session.
 *
 * @author Rsl1122
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

        session.playerKilled(new PlayerKill(killer, victim, weaponName, time));
    }
}
