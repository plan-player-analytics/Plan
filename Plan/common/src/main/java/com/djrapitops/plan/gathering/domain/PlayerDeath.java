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
package com.djrapitops.plan.gathering.domain;

import com.djrapitops.plan.delivery.domain.DateHolder;

import java.util.UUID;

/**
 * @deprecated Use {@link PlayerKill} instead.
 */
@Deprecated
public class PlayerDeath implements DateHolder {

    private final UUID killer;
    private final String killerName;
    private final long date;
    private final String weapon;

    public PlayerDeath(UUID killer, String killerName, String weapon, long date) {
        this.killer = killer;
        this.killerName = killerName;
        this.date = date;
        this.weapon = weapon;
    }

    public UUID getKiller() {
        return killer;
    }

    public String getKillerName() {
        return killerName;
    }

    @Override
    public long getDate() {
        return date;
    }

    public String getWeapon() {
        return weapon;
    }
}
