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

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
import com.djrapitops.plan.gathering.cache.SessionCache;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.processing.CriticalRunnable;

/**
 * Processor Class for KillEvent information when the killer is a
 * player.
 * <p>
 * Adds PlayerKill or a Mob kill to the active Session.
 *
 * @author AuroraLS3
 */
public class PlayerKillProcessor implements CriticalRunnable {

    private final PlayerKill.Killer killer;
    private final PlayerKill.Victim victim;
    private final ServerIdentifier server;
    private final String weaponName;
    private final long time;

    public PlayerKillProcessor(PlayerKill.Killer killer, PlayerKill.Victim victim, ServerIdentifier server, String weaponName, long time) {
        this.killer = killer;
        this.victim = victim;
        this.server = server;
        this.weaponName = weaponName;
        this.time = time;
    }

    @Override
    public void run() {
        SessionCache.getCachedSession(killer.getUuid())
                .ifPresent(session -> session.addPlayerKill(
                        new PlayerKill(killer, victim, server, weaponName, time)
                ));
    }
}
