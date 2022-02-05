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
package com.djrapitops.plan.delivery.domain.keys;

import com.djrapitops.plan.delivery.domain.mutators.PlayersMutator;
import com.djrapitops.plan.delivery.domain.mutators.SessionsMutator;
import com.djrapitops.plan.delivery.domain.mutators.TPSMutator;
import com.djrapitops.plan.gathering.domain.*;

import java.util.*;

/**
 * Class holding Key objects that are commonly used across multiple DataContainers.
 *
 * @author AuroraLS3
 */
public class CommonKeys {

    private CommonKeys() {
        /* Static variable class */
    }

    public static final Key<UUID> UUID = new Key<>(UUID.class, "uuid");
    public static final Key<UUID> SERVER_UUID = new Key<>(UUID.class, "server_uuid");
    public static final Key<String> NAME = new Key<>(String.class, "name");
    public static final PlaceholderKey<Long> REGISTERED = new PlaceholderKey<>(Long.class, "registered");
    public static final Key<List<Ping>> PING = new Key<>(new Type<List<Ping>>() {}, "ping");

    public static final Key<List<FinishedSession>> SESSIONS = new Key<>(new Type<List<FinishedSession>>() {}, "sessions");
    public static final Key<WorldTimes> WORLD_TIMES = new Key<>(WorldTimes.class, "world_times");
    public static final PlaceholderKey<Long> LAST_SEEN = new PlaceholderKey<>(Long.class, "lastSeen");

    @Deprecated
    public static final Key<List<PlayerDeath>> PLAYER_DEATHS = new Key<>(new Type<List<PlayerDeath>>() {}, "player_deaths");
    public static final Key<List<PlayerKill>> PLAYER_KILLS = new Key<>(new Type<List<PlayerKill>>() {}, "player_kills");
    public static final Key<Integer> PLAYER_KILL_COUNT = new Key<>(Integer.class, "player_kill_count");
    public static final Key<Integer> PLAYER_DEATH_COUNT = new Key<>(Integer.class, "player_death_count");
    public static final Key<Integer> MOB_KILL_COUNT = new Key<>(Integer.class, "mob_kill_count");
    public static final Key<Integer> MOB_DEATH_COUNT = new Key<>(Integer.class, "mob_death_count");
    public static final Key<Integer> DEATH_COUNT = new Key<>(Integer.class, "death_count");

    public static final Key<Boolean> BANNED = new Key<>(Boolean.class, "banned");
    public static final Key<Boolean> OPERATOR = new Key<>(Boolean.class, "operator");
    public static final Key<String> JOIN_ADDRESS = new Key<>(String.class, "join_address");

    public static final Key<SessionsMutator> SESSIONS_MUTATOR = new Key<>(SessionsMutator.class, "SESSIONS_MUTATOR");
    public static final Key<TPSMutator> TPS_MUTATOR = new Key<>(TPSMutator.class, "TPS_MUTATOR");
    public static final Key<PlayersMutator> PLAYERS_MUTATOR = new Key<>(PlayersMutator.class, "PLAYERS_MUTATOR");

    public static final Key<TreeMap<Long, Map<String, Set<UUID>>>> ACTIVITY_DATA = new Key<>(new Type<TreeMap<Long, Map<String, Set<UUID>>>>() {}, "ACTIVITY_DATA");

}