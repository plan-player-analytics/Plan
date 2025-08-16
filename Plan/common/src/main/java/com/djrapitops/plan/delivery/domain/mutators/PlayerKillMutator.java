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

import com.djrapitops.plan.delivery.domain.ServerIdentifier;
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
                    PlayerKill.Killer killer = kill.getKiller();
                    PlayerKill.Victim victim = kill.getVictim();
                    ServerIdentifier server = kill.getServer();

                    Map<String, Object> killMap = new HashMap<>();
                    killMap.put("date", kill.getDate());
                    killMap.put("killer", killer.getName());
                    killMap.put("victim", victim.getName());
                    killMap.put("killerUUID", killer.getUuid().toString());
                    killMap.put("victimUUID", victim.getUuid().toString());
                    killMap.put("killerName", killer.getName());
                    killMap.put("victimName", victim.getName());
                    killMap.put("serverUUID", server.getUuid().toString());
                    killMap.put("serverName", server.getName());
                    killMap.put("weapon", kill.getWeapon());
                    long timeSinceRegister = kill.getDate() - kill.getVictim().getRegisterDate();
                    killMap.put("timeSinceRegisterMillis", timeSinceRegister);
                    killMap.put("timeSinceRegisterFormatted", formatters.secondLong().apply(timeSinceRegister));
                    return killMap;
                }
        );
    }
}