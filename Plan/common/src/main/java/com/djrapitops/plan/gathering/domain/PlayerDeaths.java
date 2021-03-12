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

import java.util.ArrayList;
import java.util.List;

public class PlayerDeaths {

    private final DeathCounter deathCounter;
    private final List<PlayerKill> deaths;

    public PlayerDeaths(DeathCounter deathCounter) {
        this(deathCounter, new ArrayList<>());
    }

    public PlayerDeaths(DeathCounter deathCounter, List<PlayerKill> deaths) {
        this.deathCounter = deathCounter;
        this.deaths = deaths;
    }

    public void add(PlayerKill kill) {
        deaths.add(kill);
    }

    public List<PlayerKill> playerCausedDeathsAsList() {
        return deaths;
    }

    public int getDeathCount() {
        return deathCounter.getCount();
    }

    public int getPlayerCausedDeathCount() {
        return playerCausedDeathsAsList().size();
    }

    public int getMobCausedDeathCount() {
        return getDeathCount() - getPlayerCausedDeathCount();
    }
}
