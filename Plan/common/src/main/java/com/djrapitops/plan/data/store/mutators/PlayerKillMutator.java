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
package com.djrapitops.plan.data.store.mutators;

import com.djrapitops.plan.data.container.PlayerKill;
import com.djrapitops.plan.utilities.formatting.Formatters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mutator functions for {@link com.djrapitops.plan.data.container.PlayerKill} objects.
 *
 * @author Rsl1122
 */
public class PlayerKillMutator {

    private final List<PlayerKill> kills;

    public PlayerKillMutator(List<PlayerKill> kills) {
        this.kills = kills;
    }

    public PlayerKillMutator filterNonSelfKills() {
        return new PlayerKillMutator(kills.stream().filter(PlayerKill::isNotSelfKill).collect(Collectors.toList()));
    }

    public List<Map<String, Object>> toJSONAsMap(Formatters formatters) {
        return kills.stream().map(
                kill -> {
                    Map<String, Object> killMap = new HashMap<>();
                    killMap.put("date", formatters.secondLong().apply(kill.getDate()));
                    killMap.put("victim", kill.getVictimName().orElse(kill.getVictim().toString()));
                    killMap.put("killer", kill.getKillerName().orElse("Missing UUID")); // TODO Kills should support killer UUID
                    killMap.put("weapon", kill.getWeapon());
                    return killMap;
                }
        ).collect(Collectors.toList());
    }
}