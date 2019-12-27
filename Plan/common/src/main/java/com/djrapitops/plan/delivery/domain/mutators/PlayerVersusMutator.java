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
package com.djrapitops.plan.delivery.domain.mutators;

import com.djrapitops.plan.delivery.domain.DateHolder;
import com.djrapitops.plan.delivery.domain.container.DataContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.utilities.Predicates;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PlayerVersusMutator {

    private final SessionsMutator sessionsMutator;
    private final List<PlayerKill> kills;
    private final List<PlayerKill> deaths;

    public PlayerVersusMutator(SessionsMutator sessionsMutator, List<PlayerKill> kills, List<PlayerKill> deaths) {
        this.sessionsMutator = sessionsMutator;
        this.kills = kills;
        this.deaths = deaths;
    }

    public static PlayerVersusMutator forContainer(DataContainer container) {
        return new PlayerVersusMutator(
                SessionsMutator.forContainer(container),
                container.getValue(PlayerKeys.PLAYER_KILLS).orElse(Collections.emptyList()),
                container.getValue(PlayerKeys.PLAYER_DEATHS_KILLS).orElse(Collections.emptyList())
        );
    }

    public PlayerVersusMutator filterBetween(long after, long before) {
        Predicate<DateHolder> killWithinDate = Predicates.within(after, before);

        return new PlayerVersusMutator(
                sessionsMutator.filterSessionsBetween(after, before),
                Lists.filter(kills, killWithinDate),
                Lists.filter(deaths, killWithinDate)
        );
    }

    public int toPlayerKillCount() {
        return kills.size();
    }

    public int toMobKillCount() {
        return sessionsMutator.toMobKillCount();
    }

    public double toKillDeathRatio() {
        int deathCount = toPlayerDeathCount();
        return toPlayerKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public double toMobKillDeathRatio() {
        int deathCount = toMobDeathCount();
        return toMobKillCount() * 1.0 / (deathCount != 0 ? deathCount : 1);
    }

    public int toPlayerDeathCount() {
        return deaths.size();
    }

    public int toMobDeathCount() {
        return toDeathCount() - toPlayerDeathCount();
    }

    public int toDeathCount() {
        return sessionsMutator.toDeathCount();
    }

    public List<String> toTopWeapons(int limit) {
        return kills.stream()
                .map(PlayerKill::getWeapon)
                .collect(HashMap<String, Integer>::new, (map, weapon) -> map.put(weapon, map.getOrDefault(weapon, 0) + 1), (mapOne, mapTwo) -> {
                }) // Collected to Map with weapon counts
                .entrySet().stream()
                .sorted((one, two) -> Integer.compare(two.getValue(), one.getValue())) // Highest first
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
