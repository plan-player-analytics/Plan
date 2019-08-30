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
package com.djrapitops.plan.system.delivery.domain.mutators;

import com.djrapitops.plan.system.delivery.domain.container.DataContainer;
import com.djrapitops.plan.system.gathering.domain.Session;

import java.util.List;

public class PvpInfoMutator {

    private final SessionsMutator sessionsMutator;

    private PvpInfoMutator(SessionsMutator sessionsMutator) {
        this.sessionsMutator = sessionsMutator;
    }

    public PvpInfoMutator(List<Session> sessions) {
        this(new SessionsMutator(sessions));
    }

    public static PvpInfoMutator forContainer(DataContainer container) {
        return new PvpInfoMutator(SessionsMutator.forContainer(container));
    }

    public static PvpInfoMutator forMutator(SessionsMutator sessionsMutator) {
        return new PvpInfoMutator(sessionsMutator);
    }

    public double killDeathRatio() {
        int deathCount = sessionsMutator.toPlayerDeathCount();
        return sessionsMutator.toPlayerKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public int mobCausedDeaths() {
        return sessionsMutator.toDeathCount() - sessionsMutator.toPlayerDeathCount();
    }

    public double mobKillDeathRatio() {
        int deathCount = mobCausedDeaths();
        return sessionsMutator.toMobKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public int mobKills() {
        return sessionsMutator.toMobKillCount();
    }

    public int playerKills() {
        return sessionsMutator.toPlayerKillCount();
    }

    public int deaths() {
        return sessionsMutator.toDeathCount();
    }

    public int playerCausedDeaths() {
        return sessionsMutator.toPlayerDeathCount();
    }
}
