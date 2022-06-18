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
import com.djrapitops.plan.gathering.domain.ActiveSession;
import com.djrapitops.plan.processing.CriticalRunnable;

import java.util.UUID;

/**
 * Processor Class for KillEvent information when the killer is a
 * player.
 * <p>
 * Adds PlayerKill or a Mob kill to the active Session.
 *
 * @author AuroraLS3
 */
public class MobKillProcessor implements CriticalRunnable {

    private final UUID uuid;

    /**
     * Constructor.
     *
     * @param uuid UUID of the killer.
     */
    public MobKillProcessor(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public void run() {
        SessionCache.getCachedSession(uuid).ifPresent(ActiveSession::addMobKill);
    }
}
