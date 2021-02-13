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

import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.gathering.domain.PlayerKill;
import com.djrapitops.plan.utilities.java.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutator functions for {@link PlayerKill} objects.
 *
 * @author AuroraLS3
 */
public class PlayerKillMutator {

    private final List<PlayerKill> kills;

    public PlayerKillMutator(List<PlayerKill> kills) {
        this.kills = kills;
    }

    public PlayerKillMutator filterNonSelfKills() {
        return new PlayerKillMutator(Lists.filter(kills, PlayerKill::isNotSelfKill));
    }

    public List<Map<String, Object>> toJSONAsMap(Formatters formatters) {
        return Lists.map(kills,
                kill -> {
                    Map<String, Object> killMap = new HashMap<>();
                    killMap.put("date", formatters.secondLong().apply(kill.getDate()));
                    killMap.put("victim", kill.getVictimName().orElse(kill.getVictim().toString()));
                    killMap.put("killer", kill.getKillerName().orElse(kill.getKiller().toString()));
                    killMap.put("weapon", kill.getWeapon());
                    return killMap;
                }
        );
    }
}